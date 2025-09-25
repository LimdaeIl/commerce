package com.friday.commerce.user.application.service;

import com.friday.commerce.core.utils.snowflake.Snowflake;
import com.friday.commerce.user.application.dto.request.SignUpRequest;
import com.friday.commerce.user.application.dto.request.SignUpRequest.Agreement;
import com.friday.commerce.user.application.dto.response.SignUpResponse;
import com.friday.commerce.user.application.usecase.UserUseCase;
import com.friday.commerce.user.domain.entity.User;
import com.friday.commerce.user.domain.entity.UserAddress;
import com.friday.commerce.user.domain.entity.UserAgreement;
import com.friday.commerce.user.domain.exception.UserErrorCode;
import com.friday.commerce.user.domain.exception.UserException;
import com.friday.commerce.user.domain.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j(topic = "UserService")
@RequiredArgsConstructor
@Service
class UserService implements UserUseCase {

    private final Snowflake snowflake;
    private final PasswordEncoder passwordEncoder;
    private final JpaUserRepository userRepository;


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

    private void existsUserByEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserException(UserErrorCode.EMAIL_DUPLICATED);
        }
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
}

