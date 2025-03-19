package _1danhebojo.coalarm.coalarm_service.domain.user.controller;

import _1danhebojo.coalarm.coalarm_service.domain.auth.service.JwtBlacklistService;
import _1danhebojo.coalarm.coalarm_service.domain.user.controller.request.UserUpdateRequest;
import _1danhebojo.coalarm.coalarm_service.domain.user.controller.response.UserDTO;
import _1danhebojo.coalarm.coalarm_service.domain.user.service.RefreshTokenService;
import _1danhebojo.coalarm.coalarm_service.domain.user.service.UserService;
import _1danhebojo.coalarm.coalarm_service.global.api.ApiException;
import _1danhebojo.coalarm.coalarm_service.global.api.AppHttpStatus;
import _1danhebojo.coalarm.coalarm_service.global.api.BaseResponse;
import _1danhebojo.coalarm.coalarm_service.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtBlacklistService jwtBlacklistService;


    @GetMapping()
    public ResponseEntity<BaseResponse<UserDTO>> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
        String kakaoId = userDetails.getUsername();
        UserDTO userDTO = userService.findByKakaoId(kakaoId);
        return ResponseEntity.ok(BaseResponse.success(userDTO));
    }

    // NOTE: 프로필 수정
    @PatchMapping()
    public ResponseEntity<BaseResponse<Long>> updateUserInfo(@AuthenticationPrincipal UserDetails userDetails,
         @RequestPart(value = "nickname", required = false) String nickname,
         @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        String kakaoId = userDetails.getUsername();
        UserUpdateRequest request = UserUpdateRequest.of(nickname, profileImage);
        UserDTO userDTO = userService.updateUser(kakaoId, request);

        return ResponseEntity.ok(BaseResponse.success(userDTO.getUserId()));
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(@AuthenticationPrincipal UserDetails userDetails,
                                                     @RequestHeader("Authorization") String authorizationHeader) {
        if (userDetails == null) {
            throw new ApiException(AppHttpStatus.UNAUTHORIZED);
        }

        String kakaoId = userDetails.getUsername();
        Long userId = userService.findByKakaoId(kakaoId).getUserId();

        // 리프레시 토큰 삭제
        refreshTokenService.deleteRefreshToken(userId);

        // 액세스 토큰을 블랙리스트에 추가
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String accessToken = authorizationHeader.substring(7).trim();
            Instant expiryInstant = jwtTokenProvider.getExpirationInstant(accessToken);

            // 블랙리스트에 등록
            if (expiryInstant != null) {
                jwtBlacklistService.addToBlacklist(accessToken, expiryInstant);
            }
        }

        return ResponseEntity.ok(BaseResponse.success());
    }

    @DeleteMapping
    public ResponseEntity<BaseResponse<Void>> withdraw(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader("Authorization") String authorizationHeader) {

        if (userDetails == null) {
            throw new ApiException(AppHttpStatus.UNAUTHORIZED);
        }

        String kakaoId = userDetails.getUsername();
        Long userId = userService.findByKakaoId(kakaoId).getUserId();

        // 회원 탈퇴 서비스 실행 (리프레시 토큰 삭제, 블랙리스트 추가, 유저 삭제 포함)
        userService.deleteUser(userId, authorizationHeader);


        return ResponseEntity.ok(BaseResponse.success());
    }
}
