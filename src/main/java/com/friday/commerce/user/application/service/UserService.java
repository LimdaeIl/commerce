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

    private final Snowflake snowflake;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final UserCacheRepository userCacheRepository;
    private final EmailVerificationRepositoryPort emailVerificationRepositoryPort;
    private final EmailVerificationPolicy emailVerificationPolicy;
    private final MailSenderPort mailSender;
    private final EmailTemplateRendererPort templateRenderer;


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

    private void verifyUserPassword(String password, String confirmPassword) {
        if (!passwordEncoder.matches(password, confirmPassword)) {
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
        final String email = request.email().trim().toLowerCase(Locale.ROOT);

        // 이미 등록된 이메일이면 예외 발생
        existsUserByEmail(email);

        // 차단 상태 확인
        if (emailVerificationRepositoryPort.isBlocked(email)) {
            throw new UserException(UserErrorCode.EMAIL_VERIFICATION_BLOCKED);
        }

        // 쿨타임 체크
        if (emailVerificationRepositoryPort.inCooltime(email)) {
            throw new UserException(UserErrorCode.EMAIL_VERIFICATION_COOLTIME);
        }

        // 코드 6자리 생성
        String code = generate6DigitCode();

        // 코드 저장(코드 암호화)
        emailVerificationRepositoryPort.saveCode(
                email,
                passwordEncoder.encode(code),
                emailVerificationPolicy.codeValidityDuration()
        );

        // 쿨타임 세팅
        emailVerificationRepositoryPort.setCooltime(
                email,
                emailVerificationPolicy.resendCooldownDuration()
        );

        // 렌더링
        long minutes = Math.max(emailVerificationPolicy.codeValidityDuration().toMinutes(), 1);
        String html = templateRenderer.render(
                "verification-code",
                Map.of("brand", "commerce", "code", code, "minutes", minutes)
        );

        // 메일 발송
        mailSender.send(
                new EmailMessage(
                        null,  // from이 null이면 spring.mail.username 사용
                        email,
                        "[ commerce ] 이메일 인증코드",
                        html
                ));

        // 응답
        return SendCodeEmailResponse.of(email, minutes);
    }

    @Transactional
    @Override
    public VerifyCodeEmailResponse verifyCodeEmail(VerifyCodeEmailRequest request) {
        final String email = request.email().trim().toLowerCase(Locale.ROOT);

        // 차단 상태 확인
        if (emailVerificationRepositoryPort.isBlocked(email)) {
            throw new UserException(UserErrorCode.EMAIL_VERIFICATION_BLOCKED);
        }

        // 코드 조회
        String storedCode = emailVerificationRepositoryPort.getCode(email)
                .orElseThrow(
                        () -> new UserException(UserErrorCode.EMAIL_VERIFICATION_NOT_REQUESTED));

        // 코드 검증
        String input = String.valueOf(request.code());
        boolean ok = passwordEncoder.matches(input, storedCode);

        // 코드가 틀린 경우
        if (!ok) {
            long attempts = emailVerificationRepositoryPort.incrementAttempts(
                    email, emailVerificationPolicy.codeValidityDuration()
            );

            // 최대 코드 검증 횟수 초과하면 일정 시간만큼 Lock
            if (attempts >= emailVerificationPolicy.maxAttemptCount()) {
                emailVerificationRepositoryPort.block(email,
                        emailVerificationPolicy.lockoutDuration());

                emailVerificationRepositoryPort.deleteCode(email);
                emailVerificationRepositoryPort.resetAttempts(email);
            }
            throw new UserException(UserErrorCode.EMAIL_VERIFY_CODE_MISMATCH);
        }

        // 코드 검증 성공 후 처리
        emailVerificationRepositoryPort.deleteCode(email);
        emailVerificationRepositoryPort.resetAttempts(email);
        emailVerificationRepositoryPort.clearVerified(email);
        emailVerificationRepositoryPort.markVerified(email,
                emailVerificationPolicy.successValidityDuration());

        // 인증 성공 유지 시간
        long verifiedUntil =
                System.currentTimeMillis() + emailVerificationPolicy.successValidityDuration()
                        .toMillis();

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

    // 검증 + 블랙리스트 등록 + RT 삭제까지 한 번에
    private void invalidateSession(@Nullable String authHeader, String rt) {
        // RT JTI, 회원 ID 추출
        Long userId = tokenProvider.getRtUserId(rt);
        String rtJti = tokenProvider.getRtJti(rt);

        // RT JTI 일치 여부 확인(세션 탈취 방지를 위한 핵심)
        String storedRtJti = userCacheRepository.getRtJti(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.RT_NOT_FOUND));
        if (!storedRtJti.equals(rtJti)) throw new UserException(UserErrorCode.RT_JTI_INCORRECT);

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

