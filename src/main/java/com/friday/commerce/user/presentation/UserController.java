package com.friday.commerce.user.presentation;

import com.friday.commerce.core.security.annotation.CurrentUser;
import com.friday.commerce.core.security.annotation.RequireRole;
import com.friday.commerce.core.security.model.CurrentUserInfo;
import com.friday.commerce.core.security.model.UserRole;
import com.friday.commerce.user.application.dto.auth.response.SendCodeEmailResponse;
import com.friday.commerce.user.application.dto.user.request.RegisterAddressRequest;
import com.friday.commerce.user.application.dto.user.request.SoftDeleteUserRequest;
import com.friday.commerce.user.application.dto.user.request.UpdateEmailConfirmRequest;
import com.friday.commerce.user.application.dto.user.request.UpdateEmailRequest;
import com.friday.commerce.user.application.dto.user.request.UpdatePasswordRequest;
import com.friday.commerce.user.application.dto.user.response.GetUserResponse;
import com.friday.commerce.user.application.usecase.UserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "BearerAuth")
@Tag(name = "User-V1", description = "회원 관련 API")
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@RestController
public class UserController {

    private final UserUseCase userUseCase;

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = GetUserResponse.class)))
    })
    @RequireRole({UserRole.USER, UserRole.SELLER, UserRole.ADMIN})
    @GetMapping("/me")
    public ResponseEntity<GetUserResponse> getMe(
            @Parameter(hidden = true)
            @CurrentUser CurrentUserInfo info
    ) {
        GetUserResponse response = userUseCase.getUser(info);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @Operation(summary = "비밀번호 변경", description = "현재 사용자의 비밀번호를 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "변경 성공",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @RequireRole({UserRole.USER, UserRole.SELLER, UserRole.ADMIN})
    @PatchMapping("/password")
    public ResponseEntity<Void> updatePassword(
            @Parameter(hidden = true)
            @RequestHeader(name = "Authorization", required = false, defaultValue = "") String authHeader,
            @Parameter(hidden = true)
            @CurrentUser CurrentUserInfo info,
            @RequestBody @Valid UpdatePasswordRequest request
    ) {
        userUseCase.updatePassword(authHeader, info, request);

        return ResponseEntity
                .noContent()
                .build();
    }

    @Operation(summary = "이메일 변경 요청", description = "이메일 변경을 요청하고 인증 코드를 전송합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "요청 성공(코드 발송)",
                    content = @Content(schema = @Schema(implementation = SendCodeEmailResponse.class)))
    })
    @RequireRole({UserRole.USER, UserRole.SELLER, UserRole.ADMIN})
    @PatchMapping("/email")
    public ResponseEntity<SendCodeEmailResponse> updateEmail(
            @Parameter(hidden = true)
            @CurrentUser CurrentUserInfo info,
            @RequestBody @Valid UpdateEmailRequest request
    ) {
        SendCodeEmailResponse response = userUseCase.updateEmail(info, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @Operation(summary = "이메일 변경 확정", description = "인증 코드를 검증하여 이메일 변경을 확정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "확정 성공",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @RequireRole({UserRole.USER, UserRole.SELLER, UserRole.ADMIN})
    @PostMapping("/email/confirm")
    public ResponseEntity<Void> confirmEmailChange(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @CurrentUser CurrentUserInfo info,
            @Valid @RequestBody UpdateEmailConfirmRequest request
    ) {
        userUseCase.confirmUpdateEmail(authHeader, info, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "주소 등록", description = "사용자의 새 배송지 주소를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "등록 성공",
                    content = @Content(schema = @Schema(implementation = GetUserResponse.class)))
    })
    @RequireRole({UserRole.USER, UserRole.SELLER, UserRole.ADMIN})
    @PostMapping("/addresses")
    public ResponseEntity<GetUserResponse> registerAddress(
            @Parameter(hidden = true)
            @CurrentUser CurrentUserInfo info,
            @Valid @RequestBody RegisterAddressRequest request
    ) {
        GetUserResponse response = userUseCase.registerAddress(info, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }


    @Operation(summary = "기본 주소 설정", description = "특정 주소를 기본 주소로 설정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "설정 성공",
                    content = @Content(schema = @Schema(implementation = GetUserResponse.class)))
    })
    @RequireRole({UserRole.USER, UserRole.SELLER, UserRole.ADMIN})
    @PostMapping("/addresses/{addressId}/default")
    public ResponseEntity<GetUserResponse> updateDefaultAddress(
            @PathVariable Long addressId,
            @Parameter(hidden = true)
            @CurrentUser CurrentUserInfo info) {
        GetUserResponse response = userUseCase.updateDefaultAddress(info, addressId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }


    @Operation(summary = "주소 삭제", description = "특정 주소를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공",
                    content = @Content(schema = @Schema(implementation = GetUserResponse.class)))
    })
    @RequireRole({UserRole.USER, UserRole.SELLER, UserRole.ADMIN})
    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<GetUserResponse> deleteAddress(
            @PathVariable Long addressId,
            @Parameter(hidden = true)
            @CurrentUser CurrentUserInfo info
    ) {
        GetUserResponse response = userUseCase.deleteAddress(info, addressId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @Operation(summary = "회원 탈퇴(소프트 삭제)", description = "현재 사용자를 소프트 삭제 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "탈퇴 성공",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @RequireRole({UserRole.USER, UserRole.SELLER, UserRole.ADMIN})
    @DeleteMapping("/me")
    public ResponseEntity<Void> softDeleteUser(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid SoftDeleteUserRequest request,
            @Parameter(hidden = true)
            @CurrentUser CurrentUserInfo info
    ) {
        userUseCase.softDeleteUser(info, authHeader, request);

        return ResponseEntity
                .noContent()
                .build();
    }
}
