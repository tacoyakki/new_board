package com.demomo.member.controller;

import com.demomo.member.dto.LoginRequest;
import com.demomo.member.dto.SignupRequest;
import com.demomo.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public String signup(@RequestBody SignupRequest signupRequest) {
        memberService.signup(signupRequest);
        return "회원가입성공!!!!!!!!!!!!!";
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest loginRequest) {

        return memberService.login(loginRequest);
    }

    @PostMapping("/reissue")
    public String reissue(@RequestHeader("RefreshToken") String refreshToken) {

        return memberService.reissue(refreshToken);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication) {

        String username = authentication.getName();
        memberService.logout(username);
        return ResponseEntity.ok("로그아웃했어용~");

    }
}
