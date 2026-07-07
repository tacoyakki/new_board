package com.demomo.member.controller;

import com.demomo.member.dto.LoginRequest;
import com.demomo.member.dto.SignupRequest;
import com.demomo.member.dto.AuthResponse;
import com.demomo.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

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
}

