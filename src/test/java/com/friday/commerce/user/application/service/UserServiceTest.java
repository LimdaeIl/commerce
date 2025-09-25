package com.friday.commerce.user.application.service;

import com.friday.commerce.core.utils.snowflake.Snowflake;
import com.friday.commerce.user.application.dto.request.SignUpRequest;
import com.friday.commerce.user.application.dto.request.SignUpRequest.Address;
import com.friday.commerce.user.application.dto.request.SignUpRequest.Agreement;
import com.friday.commerce.user.application.dto.response.SignUpResponse;
import com.friday.commerce.user.domain.entity.User;
import com.friday.commerce.user.domain.exception.UserException;
import com.friday.commerce.user.domain.repository.JpaUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;




@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock JpaUserRepository jpaUserRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock
    Snowflake snowflake;

    @InjectMocks UserService userService;

    @Captor ArgumentCaptor<User> userCaptor;

    private SignUpRequest validRequest() {
        return SignUpRequest.builder()
                .email("alice@example.com")
                .password("example123!")
                .username("alice")
                .agreement(new Agreement(true, true, true))
                .address(new Address("12521", "주소1", "주소2", "서울", "강남"))
                .build();
    }

    @DisplayName("given 유효한 가입요청 when signUp then 비밀번호는 해시로 저장되고 응답에 ID가 담긴다")
    @Test
    void givenValidRequest_whenSignUp_thenPasswordHashedAndIdReturned() {
        // given
        SignUpRequest req = validRequest();
        given(jpaUserRepository.existsByEmail(req.email())).willReturn(false);
        given(passwordEncoder.encode(req.password())).willReturn("ENCODED_HASH");
        given(snowflake.nextId()).willReturn(1L);
        // save가 null을 반환하지 않도록 저장되는 엔티티 그대로 반환
        given(jpaUserRepository.save(any(User.class))).willAnswer(inv ->
                inv.getArgument(0)
        );

        // when
        SignUpResponse resp = userService.signUp(req);

        // then
        verify(jpaUserRepository).existsByEmail(req.email());
        verify(passwordEncoder).encode(req.password());
        verify(jpaUserRepository).save(userCaptor.capture());

        User saved = userCaptor.getValue();
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getPassword()).isEqualTo("ENCODED_HASH");
        assertThat(saved.getPassword()).isNotEqualTo(req.password());

        assertThat(resp.userId()).isEqualTo(1L);
        assertThat(resp.username()).isEqualTo("alice");
    }

    @DisplayName("given 중복 이메일 when signUp then UserException 발생")
    @Test
    void givenDuplicatedEmail_whenSignUp_thenThrowUserException() {
        // given
        SignUpRequest req = validRequest();
        given(jpaUserRepository.existsByEmail(req.email())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.signUp(req))
                .isInstanceOf(UserException.class);
        verify(passwordEncoder, never()).encode(any());
        verify(jpaUserRepository, never()).save(any());
    }

    @DisplayName("given 필수 동의 누락 when signUp then UserException 발생")
    @Test
    void givenMissingRequiredAgreements_whenSignUp_thenThrowUserException() {
        // given (이용약관 미동의)
        SignUpRequest req = SignUpRequest.builder()
                .email("bob@example.com")
                .password("example123!")
                .username("bob")
                .agreement(new Agreement(false, true, true)) // 서비스 규칙상 필수
                .address(new Address("12521", "주소1", "주소2", "서울", "강남"))
                .build();

        given(jpaUserRepository.existsByEmail(req.email())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.signUp(req))
                .isInstanceOf(UserException.class);
        verify(jpaUserRepository, never()).save(any());
    }
}
