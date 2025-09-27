package com.friday.commerce.user.application.service;

import com.friday.commerce.core.utils.snowflake.Snowflake;
import com.friday.commerce.user.application.dto.request.LogoutRequest;
import com.friday.commerce.user.application.dto.request.ReIssueRequest;
import com.friday.commerce.user.application.dto.request.SignInRequest;
import com.friday.commerce.user.application.dto.request.SignUpRequest;
import com.friday.commerce.user.application.dto.request.SignUpRequest.Agreement;
import com.friday.commerce.user.application.dto.response.ReIssueResponse;
import com.friday.commerce.user.application.dto.response.SignInResponse;
import com.friday.commerce.user.application.dto.response.SignUpResponse;
import com.friday.commerce.user.application.usecase.UserUseCase;
import com.friday.commerce.user.domain.entity.User;
import com.friday.commerce.user.domain.entity.UserAddress;
import com.friday.commerce.user.domain.entity.UserAgreement;
import com.friday.commerce.user.domain.exception.UserErrorCode;
import com.friday.commerce.user.domain.exception.UserException;
import com.friday.commerce.user.domain.port.TokenProvider;
import com.friday.commerce.user.domain.port.UserCacheRepository;
import com.friday.commerce.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j(topic = "UserService")
@RequiredArgsConstructor
@Service
class UserService implements UserUseCase {

    private final Snowflake snowflake;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final UserCacheRepository userCacheRepository;


    private void verifyAgreement(Agreement agreement) {
        if (!agreement.termsOfService()) {
            throw new UserException(UserErrorCode.AGREEMENT_TERMS_OF_SERVICE);
        }
        if (!agreement.privacy()) {
            throw new UserException(UserErrorCode.AGREEMENT_PRIVACY);
        }
        if (!agreement.marketing()) {
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
        existsUserByEmail(request.email());
        verifyAgreement(request.agreement());

        UserAgreement userAgreement = UserAgreement.create(
                request.agreement().termsOfService(),
                request.agreement().privacy(),
                request.agreement().marketing()
        );

        UserAddress userAddress = UserAddress.create(
                request.address().zipCode(),
                request.address().addressLine1(),
                request.address().addressLine2(),
                request.address().city(),
                request.address().state()
        );

        User user = User.create(
                snowflake.nextId(),
                request.email(),
                passwordEncoder.encode(request.password()),
                request.username(),
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
        String at = tokenProvider.issueAt(user.getUserId(), user.getUserRole());
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
        // 토큰으로부터 회원 ID 추출
        Long rtUserId = tokenProvider.getRtUserId(request.rt());

        // 토큰(at, rt)에서 jti 추출 -> JwtProvider
        String atJti = tokenProvider.getAtJti(authHeader);
        String rtJti = tokenProvider.getRtJti(request.rt());

        // 레디스 안에 RT 토큰과 요청 토큰의 jti 일치하는 지 확인 및 조회
        String getRtJti = userCacheRepository.getRtJti(rtUserId)
                .orElseThrow(() -> new UserException(UserErrorCode.RT_NOT_FOUND));

        if (!getRtJti.equals(rtJti)) {
            throw new UserException(UserErrorCode.RT_JTI_INCORRECT);
        }

        // 토큰(at, rt)에서 남은 시간 추출 -> JwtProvider
        long atTtlMs = tokenProvider.getAtTtlMs(authHeader);
        long rtTtlMs = tokenProvider.getRtTtlMs(request.rt());

        // 토큰(at, rt)을 블랙리스트로 등록 -> JwtProvider
        userCacheRepository.atSetBl(atJti, atTtlMs);
        userCacheRepository.rtSetBl(rtJti, rtTtlMs);

        // 토큰 삭제(rt) -> JwtProvider
        userCacheRepository.deleteRt(rtUserId);
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

        // 선택: 토큰(at) 블랙리스트 등록 -> JwtProvider
        if (StringUtils.hasText(authHeader)) {
            long atTtlMs = tokenProvider.getAtTtlMs(authHeader);
            String atJti = tokenProvider.getAtJti(authHeader);

            if (userCacheRepository.isAtBl(atJti)) {
                throw new UserException(UserErrorCode.AT_BLACKLIST);
            }

            userCacheRepository.atSetBl(atJti, atTtlMs);
        }

        // at 블랙리스트 등록
        userCacheRepository.rtSetBl(rtJti, rtTtlMs);

        // 토큰 삭제(rt) -> JwtProvider
        userCacheRepository.deleteRt(rtUserId);

        // 실제 유저 존재하는지 확인 + 권한 확인
        User userById = findUserById(rtUserId);

        // 토큰 발급
        String newAt = tokenProvider.issueAt(rtUserId, userById.getUserRole());
        String newRt = tokenProvider.issueRt(rtUserId);

        // jti/TTL 산출
        String newRtJti = tokenProvider.getRtJti(newRt);
        long newAtTtlMs = tokenProvider.getAtTtlMs(newAt);
        long newRtTtlMs = tokenProvider.getRtTtlMs(newRt);

        // 4) jti(RT) 저장
        userCacheRepository.saveToken(rtUserId, newRtJti, newRtTtlMs);

        return ReIssueResponse.of(userById, newAt, newRt, newAtTtlMs, newRtTtlMs);
    }
}

