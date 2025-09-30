package com.friday.commerce.user.application.service;

import com.friday.commerce.core.mail.application.port.EmailTemplateRendererPort;
import com.friday.commerce.core.mail.application.port.MailSenderPort;
import com.friday.commerce.core.mail.domain.EmailMessage;
import com.friday.commerce.core.security.model.CurrentUserInfo;
import com.friday.commerce.core.utils.snowflake.Snowflake;
import com.friday.commerce.user.application.dto.auth.request.LogoutRequest;
import com.friday.commerce.user.application.dto.auth.request.ReIssueRequest;
import com.friday.commerce.user.application.dto.auth.request.SendCodeEmailRequest;
import com.friday.commerce.user.application.dto.auth.request.SignInRequest;
import com.friday.commerce.user.application.dto.auth.request.SignUpRequest;
import com.friday.commerce.user.application.dto.auth.request.VerifyCodeEmailRequest;
import com.friday.commerce.user.application.dto.auth.response.ReIssueResponse;
import com.friday.commerce.user.application.dto.auth.response.SendCodeEmailResponse;
import com.friday.commerce.user.application.dto.auth.response.SignInResponse;
import com.friday.commerce.user.application.dto.auth.response.SignUpResponse;
import com.friday.commerce.user.application.dto.auth.response.VerifyCodeEmailResponse;
import com.friday.commerce.user.application.dto.user.request.RegisterAddressRequest;
import com.friday.commerce.user.application.dto.user.request.UpdateEmailConfirmRequest;
import com.friday.commerce.user.application.dto.user.request.UpdateEmailRequest;
import com.friday.commerce.user.application.dto.user.request.UpdatePasswordRequest;
import com.friday.commerce.user.application.dto.user.response.GetUserResponse;
import com.friday.commerce.user.application.usecase.AuthUseCase;
import com.friday.commerce.user.application.usecase.UserUseCase;
import com.friday.commerce.user.domain.entity.User;
import com.friday.commerce.user.domain.entity.UserAddress;
import com.friday.commerce.user.domain.entity.UserAgreement;
import com.friday.commerce.user.domain.exception.UserErrorCode;
import com.friday.commerce.user.domain.exception.UserException;
import com.friday.commerce.user.domain.policy.EmailVerificationPolicy;
import com.friday.commerce.user.domain.port.EmailVerificationRepositoryPort;
import com.friday.commerce.user.domain.port.TokenProvider;
import com.friday.commerce.user.domain.port.UserCacheRepository;
import com.friday.commerce.user.domain.repository.UserRepository;
import jakarta.annotation.Nullable;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j(topic = "UserService")
@RequiredArgsConstructor
@Service
class UserService implements AuthUseCase, UserUseCase {

    private static final String BRAND = "commerce";
    private static final String TEMPLATE_EMAIL_VERIFICATION = "verification-code";
    private static final String SUBJECT_SIGNUP = "[ commerce ] 회원가입 이메일 인증코드";
    private static final String SUBJECT_UPDATE = "[ commerce ] 이메일 변경 인증코드";

    private final Snowflake snowflake;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final UserCacheRepository userCacheRepository;
    private final EmailVerificationRepositoryPort emailVerificationRepositoryPort;
    private final EmailVerificationPolicy emailVerificationPolicy;
    private final MailSenderPort mailSender;
    private final EmailTemplateRendererPort templateRenderer;

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void ensureNotBlocked(String email) {
        if (emailVerificationRepositoryPort.isBlocked(email)) {
            throw new UserException(UserErrorCode.EMAIL_VERIFICATION_BLOCKED);
        }
    }

    private void ensureNotInCoolTime(String email) {
        if (emailVerificationRepositoryPort.inCooltime(email)) {
            throw new UserException(UserErrorCode.EMAIL_VERIFICATION_COOLTIME);
        }
    }

    /**
     * 인증코드를 생성/저장하고, 쿨타임을 설정한 뒤 이메일을 발송한다.
     *
     * @return minutes (유효 시간, 분)
     */
    private long createAndSendEmailCode(String email, String subject, String purpose) {
        ensureNotBlocked(email);
        ensureNotInCoolTime(email);

        // 코드 생성 및 저장
        String code = generate6DigitCode();
        emailVerificationRepositoryPort.saveCode(
                email,
                passwordEncoder.encode(code),
                emailVerificationPolicy.codeValidityDuration()
        );

        // 쿨타임 설정
        emailVerificationRepositoryPort.setCooltime(
                email, emailVerificationPolicy.resendCooldownDuration()
        );

        // 템플릿 렌더링
        long minutes = Math.max(emailVerificationPolicy.codeValidityDuration().toMinutes(), 1);
        String html = templateRenderer.render(
                TEMPLATE_EMAIL_VERIFICATION,
                Map.of("brand", BRAND, "code", code, "minutes", minutes, "purpose", purpose)
        );

        // 메일 발송
        mailSender.send(new EmailMessage(
                null, // null이면 spring.mail.username 사용
                email,
                subject,
                html
        ));

        return minutes;
    }

    /**
     * 입력 코드 검증 로직(횟수 증가/차단/정리 포함).
     *
     * @return verifiedUntil (millis)
     */
    private long verifyEmailCodeInternal(String email, int codeInput) {
        ensureNotBlocked(email);

        String storedCode = emailVerificationRepositoryPort.getCode(email)
                .orElseThrow(
                        () -> new UserException(UserErrorCode.EMAIL_VERIFICATION_NOT_REQUESTED));

        boolean ok = passwordEncoder.matches(String.valueOf(codeInput), storedCode);

        if (!ok) {
            long attempts = emailVerificationRepositoryPort.incrementAttempts(
                    email, emailVerificationPolicy.codeValidityDuration()
            );

            if (attempts >= emailVerificationPolicy.maxAttemptCount()) {
                // 일정 시간 Lock & 정리
                emailVerificationRepositoryPort.block(email,
                        emailVerificationPolicy.lockoutDuration());
                emailVerificationRepositoryPort.deleteCode(email);
                emailVerificationRepositoryPort.resetAttempts(email);
            }
            throw new UserException(UserErrorCode.EMAIL_VERIFY_CODE_MISMATCH);
        }

        // 성공 시 정리 + 인증표식 갱신
        emailVerificationRepositoryPort.deleteCode(email);
        emailVerificationRepositoryPort.resetAttempts(email);
        emailVerificationRepositoryPort.clearVerified(email);
        emailVerificationRepositoryPort.markVerified(
                email, emailVerificationPolicy.successValidityDuration()
        );

        return System.currentTimeMillis() + emailVerificationPolicy.successValidityDuration()
                .toMillis();
    }

    private void verifyAgreement(SignUpRequest req) {
        if (!req.agreedTos()) {
            throw new UserException(UserErrorCode.AGREEMENT_TERMS_OF_SERVICE);
        }
        if (!req.agreedPrivacy()) {
            throw new UserException(UserErrorCode.AGREEMENT_PRIVACY);
        }
        if (!req.agreedMarketing()) {
            throw new UserException(UserErrorCode.AGREEMENT_MARKETING);
        }
    }

    private User verifyUserByEmail(String email) {
        User userByEmail = findUserByEmail(email);

        if (userByEmail.getIsLocked()) {
            // 이후 3일 이내 다시 재가입 가능하도록 설정
            throw new UserException(UserErrorCode.USER_IS_LOCKED);
        }

        return userByEmail;
    }

    private void verifyUserPassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new UserException(UserErrorCode.PASSWORD_INCORRECT);
        }
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }

    private void existsUserByEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserException(UserErrorCode.EMAIL_DUPLICATED);
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    @Override
    public SignUpResponse signUp(SignUpRequest request) {
        existsUserByEmail(request.getEmail());
        verifyAgreement(request);

        UserAgreement userAgreement = UserAgreement.create(
                request.agreedTos(),
                request.agreedPrivacy(),
                request.agreedMarketing()
        );
        UserAddress userAddress = UserAddress.create(
                request.zipCode(),
                request.addressLine1(),
                request.addressLine2(),
                request.city(),
                request.state()
        );

        User user = User.create(
                snowflake.nextId(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getUsername(),
                userAgreement,
                userAddress
        );

        User savedUser = userRepository.save(user);

        return SignUpResponse.from(savedUser);
    }

    @Transactional
    @Override
    public SignInResponse signIn(SignInRequest request) {
        // 1) ID, PW 확인
        User user = verifyUserByEmail(request.email());
        verifyUserPassword(request.password(), user.getPassword());

        // 2) 토큰 발급
        String at = tokenProvider.issueAt(user.getUserId(), user.toCoreRole());
        String rt = tokenProvider.issueRt(user.getUserId());

        // 3) jti/TTL 산출
        String rtJti = tokenProvider.getRtJti(rt);
        long atTtlMs = tokenProvider.getAtTtlMs(at);
        long rtTtlMs = tokenProvider.getRtTtlMs(rt);

        // 4) jti(RT) 저장
        userCacheRepository.saveToken(user.getUserId(), rtJti, rtTtlMs);

        // 5) 응답
        return SignInResponse.of(user, at, rt, atTtlMs, rtTtlMs);
    }

    @Transactional
    @Override
    public void logout(String authHeader, LogoutRequest request) {
        invalidateSession(authHeader, request.rt());
    }

    @Transactional
    @Override
    public ReIssueResponse reIssue(String authHeader, ReIssueRequest request) {
        // 토큰으로부터 회원 ID 추출
        Long rtUserId = tokenProvider.getRtUserId(request.rt());

        // 토큰(rt)에서 jti 추출 -> JwtProvider
        String rtJti = tokenProvider.getRtJti(request.rt());

        // 블랙리스트 등록된 RT 인지 확인
        if (userCacheRepository.isRtBl(rtJti)) {
            throw new UserException(UserErrorCode.RT_BLACKLIST);
        }

        // 레디스 안에 RT 토큰과 요청 토큰의 jti 일치하는 지 확인 및 조회
        String getRtJti = userCacheRepository.getRtJti(rtUserId)
                .orElseThrow(() -> new UserException(UserErrorCode.RT_NOT_FOUND));

        if (!getRtJti.equals(rtJti)) {
            throw new UserException(UserErrorCode.RT_JTI_INCORRECT);
        }

        // 토큰(rt)에서 남은 시간 추출 -> JwtProvider
        long rtTtlMs = tokenProvider.getRtTtlMs(request.rt());

        // 선택: 토큰(at) 블랙리스트 등록 -> userCacheRepository
        if (StringUtils.hasText(authHeader)) {
            long atTtlMs = tokenProvider.getAtTtlMs(authHeader);
            String atJti = tokenProvider.getAtJti(authHeader);

            if (userCacheRepository.isAtBl(atJti)) {
                throw new UserException(UserErrorCode.AT_BLACKLIST);
            }

            userCacheRepository.atSetBl(atJti, atTtlMs);
        }

        // rt 블랙리스트 등록
        userCacheRepository.rtSetBl(rtJti, rtTtlMs);

        // 토큰 삭제(rt) -> JwtProvider
        userCacheRepository.deleteRt(rtUserId);

        // 실제 유저 존재하는지 확인 + 권한 확인
        User userById = findUserById(rtUserId);

        // 토큰 발급
        String newAt = tokenProvider.issueAt(rtUserId, userById.toCoreRole());
        String newRt = tokenProvider.issueRt(rtUserId);

        // jti/TTL 산출
        String newRtJti = tokenProvider.getRtJti(newRt);
        long newAtTtlMs = tokenProvider.getAtTtlMs(newAt);
        long newRtTtlMs = tokenProvider.getRtTtlMs(newRt);

        // 4) jti(RT) 저장
        userCacheRepository.saveToken(rtUserId, newRtJti, newRtTtlMs);

        return ReIssueResponse.of(userById, newAt, newRt, newAtTtlMs, newRtTtlMs);
    }

    @Transactional
    @Override
    public SendCodeEmailResponse sendCodeEmail(SendCodeEmailRequest request) {
        final String email = normalizeEmail(request.email());
        existsUserByEmail(email); // 이미 등록된 이메일이면 예외

        long minutes = createAndSendEmailCode(email, SUBJECT_SIGNUP, "signup");
        return SendCodeEmailResponse.of(email, minutes);
    }


    @Transactional
    @Override
    public VerifyCodeEmailResponse verifyCodeEmail(VerifyCodeEmailRequest request) {
        final String email = normalizeEmail(request.email());
        long verifiedUntil = verifyEmailCodeInternal(email, request.code());
        return VerifyCodeEmailResponse.of(email, verifiedUntil);
    }

    private String generate6DigitCode() {
        SecureRandom r = new SecureRandom();
        int n = 100000 + r.nextInt(900000);
        return String.valueOf(n);
    }

    @Transactional(readOnly = true)
    @Override
    public GetUserResponse getUser(CurrentUserInfo info) {
        User userById = findUserById(info.userId());

        return GetUserResponse.from(userById);
    }

    @Transactional
    @Override
    public void updatePassword(String authHeader, CurrentUserInfo info,
            UpdatePasswordRequest request) {
        User user = findUserById(info.userId());

        // RT 확인
        if (!StringUtils.hasText(request.rt())) {
            throw new UserException(UserErrorCode.RT_NOT_FOUND);
        }

        // 비밀번호 재확인
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UserException(UserErrorCode.PASSWORD_INCORRECT);
        }

        // 비밀번호가 이전과 동일한 지
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new UserException(UserErrorCode.PASSWORD_SAME_BEFORE);
        }

        // 새로운 비밀번호로 변경
        user.updatePassword(passwordEncoder.encode(request.newPassword()), info.userId());

        // 강제 로그아웃: 이전 AT, RT 무효화
        invalidateSession(authHeader, request.rt());
    }

    @Transactional
    @Override
    public SendCodeEmailResponse updateEmail(CurrentUserInfo info,
            UpdateEmailRequest request) {
        final String newEmail = normalizeEmail(request.newEmail());
        User user = findUserById(info.userId());

        if (user.getEmail().equals(newEmail)) {
            throw new UserException(UserErrorCode.EMAIL_SAME_BEFORE);
        }
        existsUserByEmail(newEmail); // 이미 등록된 이메일이면 예외

        long minutes = createAndSendEmailCode(newEmail, SUBJECT_UPDATE, "update");
        return SendCodeEmailResponse.of(newEmail, minutes);
    }

    @Transactional
    @Override
    public void confirmUpdateEmail(String authHeader, CurrentUserInfo info,
            UpdateEmailConfirmRequest request) {
        final String newEmail = normalizeEmail(request.newEmail());
        User user = findUserById(info.userId());

        // 자기 자신과 동일 이메일이면 예외
        if (user.getEmail().equals(newEmail)) {
            throw new UserException(UserErrorCode.EMAIL_SAME_BEFORE);
        }

        // 인증 코드 검증 (성공 시 내부적으로 코드 삭제/시도횟수 리셋/verified 마킹)
        verifyEmailCodeInternal(newEmail, request.code());

        // 최종 중복 재확인 (코드 발송과 검증 사이에 누군가 사용했을 수 있음)
        existsUserByEmail(newEmail);

        // 이메일 변경
        user.updateEmail(newEmail, info.userId());

        // 강제 로그아웃(세션 무효화) - 보안상 기존 토큰 모두 폐기
        invalidateSession(authHeader, request.rt());

        // verified 플래그 정리
        emailVerificationRepositoryPort.clearVerified(newEmail);
    }

    @Transactional
    @Override
    public GetUserResponse registerAddress(CurrentUserInfo info, RegisterAddressRequest request) {
        User user = findUserById(info.userId());

        UserAddress userAddress = UserAddress.create(
                request.zipCode(),
                request.addressLine1(),
                request.addressLine2(),
                request.city(),
                request.state()
        );

        user.addAddress(info.userId(), userAddress);
        userRepository.flush(); // INSERT 수행 → IDENTITY 키 채번 완료

        return GetUserResponse.from(user);
    }

    @Transactional
    @Override
    public GetUserResponse updateDefaultAddress(CurrentUserInfo info, Long addressId) {
        User user = findUserById(info.userId());

        // 소유권 검증
        boolean existsAddress = user.getUserAddresses().stream()
                .anyMatch(address -> addressId.equals(address.getAddressId()));
        if (!existsAddress) {
            throw new UserException(UserErrorCode.ADDRESS_NOT_FOUND); // or NOT_OWNED
        }

        user.setDefaultAddress(addressId); // 도메인 규칙: 단일 기본 주소 보장

        return GetUserResponse.from(user); // JPA 더티체킹으로 플러시됨
    }

    @Transactional
    @Override
    public GetUserResponse deleteAddress(CurrentUserInfo info, Long addressId) {
        User user = findUserById(info.userId());

        // 주소는 최소 1개
        if (user.getUserAddresses().size() <= 1) {
            throw new UserException(UserErrorCode.ADDRESS_LAST_CANNOT_DELETE);
        }

        // 내 주소인지 확인 + 타겟 조회
        UserAddress target = user.getUserAddresses().stream()
                .filter(address -> addressId.equals(address.getAddressId()))
                .findFirst()
                .orElseThrow(() -> new UserException(UserErrorCode.ADDRESS_NOT_FOUND));

        boolean wasDefault = Boolean.TRUE.equals(target.getIsDefault());

        // 삭제: 컬렉션에서 제거 + orphanRemoval=true 로 영속성 컨텍스트에서 삭제
        user.removeAddress(target);

        // 기본 주소를 지웠다면, 남아있는 첫 번째 주소를 기본으로 설정
        if (wasDefault) {
            // @OrderColumn(order_index) 이므로 0번이 "첫 번째" 주소
            UserAddress first = user.getUserAddresses().getFirst();
            user.setDefaultAddress(first.getAddressId());
        }

        // 5) 응답
        return GetUserResponse.from(user);
    }

    // 검증 + 블랙리스트 등록 + RT 삭제까지 한 번에
    private void invalidateSession(@Nullable String authHeader, String rt) {
        // RT JTI, 회원 ID 추출
        Long userId = tokenProvider.getRtUserId(rt);
        String rtJti = tokenProvider.getRtJti(rt);

        // 블랙리스트 등록된 RT 인지 확인
        if (userCacheRepository.isRtBl(rtJti)) {
            throw new UserException(UserErrorCode.RT_BLACKLIST);
        }

        // RT JTI 일치 여부 확인(세션 탈취 방지를 위한 핵심)
        String storedRtJti = userCacheRepository.getRtJti(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.RT_NOT_FOUND));
        if (!storedRtJti.equals(rtJti)) {
            throw new UserException(UserErrorCode.RT_JTI_INCORRECT);
        }

        // RT 블랙리스트 등록
        long rtTtlMs = tokenProvider.getRtTtlMs(rt);
        userCacheRepository.rtSetBl(rtJti, rtTtlMs);

        // AT가 있으면 함께 블랙리스트
        if (StringUtils.hasText(authHeader)) {
            String atJti = tokenProvider.getAtJti(authHeader);
            long atTtlMs = tokenProvider.getAtTtlMs(authHeader);
            userCacheRepository.atSetBl(atJti, atTtlMs);
        }

        userCacheRepository.deleteRt(userId);
    }
}

