package com.demomo.member.controller;

import com.demomo.member.dto.LoginRequest;
import com.demomo.member.dto.SignupRequest;
import com.demomo.member.dto.AuthResponse;
import com.demomo.member.dto.ProfileResponse;
import com.demomo.member.dto.UpdateProfileRequest;
import com.demomo.member.service.MemberService;
import com.demomo.member.service.ProfileImageService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;
    private final ProfileImageService profileImageService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest signupRequest) {
        memberService.signup(signupRequest);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest loginRequest) {

        return memberService.login(loginRequest);
    }

    @PostMapping("/reissue")
    public AuthResponse reissue(@RequestHeader("RefreshToken") String refreshToken) {

        return memberService.reissue(refreshToken);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization")String accessToken, Authentication authentication) {

        String username = authentication.getName();
        memberService.logout(accessToken, username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profiles/{username}")
    public ProfileResponse profile(@PathVariable String username) {
        return memberService.getProfile(username);
    }

    @GetMapping("/me")
    public ProfileResponse me(Authentication authentication) {
        return memberService.getProfile(authentication.getName());
    }

    @PutMapping("/me")
    public ProfileResponse updateMe(@RequestBody UpdateProfileRequest request, Authentication authentication) {
        return memberService.updateProfile(authentication.getName(), request);
    }

    @PostMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProfileResponse uploadProfileImage(@RequestPart("file") MultipartFile file,
                                              Authentication authentication) {
        String imageUrl = profileImageService.store(file);
        ProfileResponse current = memberService.getProfile(authentication.getName());
        return memberService.updateProfile(authentication.getName(),
                new UpdateProfileRequest(current.nickname(), current.bio(), imageUrl));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(@RequestHeader("Authorization") String accessToken,
                                         Authentication authentication) {
        memberService.withdraw(accessToken, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}

