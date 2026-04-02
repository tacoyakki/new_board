package com.demomo.member.service;

import com.demomo.member.domain.Member;
import com.demomo.member.dto.LoginRequest;
import com.demomo.member.repository.MemberRepository;
import com.demomo.member.dto.SignupRequest;
import com.demomo.global.security.jwt.JwtUtil;
import com.demomo.global.security.jwt.RefreshToken;
import com.demomo.global.security.jwt.RefreshTokenRepository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    // MemberService.java 수정
    private final RefreshTokenRepository refreshTokenRepository; // 주입 추가
    private final RedisTemplate<String, Object> redisTemplate;


    public String login(LoginRequest request) {
        Member member = memberRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("유저가 없지롱"));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new RuntimeException("비번이 틀렸지롱");
        }

        // 1. Access Token 발급
        String accessToken = jwtUtil.createAccessToken(member.getUsername(), member.getRole().name());

        // 2. Refresh Token 발급
        String refreshToken = jwtUtil.createRefreshToken(member.getUsername());

        // 3. Redis에 저장 (이미 있으면 덮어쓰기 됨)
        refreshTokenRepository.save(new RefreshToken(member.getUsername(), refreshToken));

        return accessToken;
    }

    public void signup(SignupRequest request) {

        if (memberRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("이미 존재하는 사용자지롱");
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        Member member = Member.builder()
                .username(request.username())
                .password(encodedPassword)
                .role(request.role())
                .build();

        memberRepository.save(member);
    }


    public Member findByUsername(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("유저가 업지롱"));
    }

    public String reissue(String refreshToken) {
        // 1. 리프레시 토큰 자체가 유효한지 확인 (만료 여부 등)
        if (!jwtUtil.isValid(refreshToken)) {
            throw new RuntimeException("리프레시 토큰이 만료되었거나 유효하지 않습니다.");
        }

        // 2. 토큰에서 유저 이름 추출
        String username = jwtUtil.getUsernameFromRefreshToken(refreshToken);

        // 3. Redis에 저장된 토큰 꺼내기
        RefreshToken savedToken = refreshTokenRepository.findById(username)
                .orElseThrow(() -> new RuntimeException("로그인 정보가 없습니다. 다시 로그인해주세요."));

        // 4. 사용자가 보낸 토큰과 Redis의 토큰이 일치하는지 대조
        if (!savedToken.getRefreshToken().equals(refreshToken)) {
            throw new RuntimeException("토큰이 일치하지 않습니다. 해킹 위험이 있습니다.");
        }

        // 5. 일치한다면 새로운 Access Token 발급
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        return jwtUtil.createAccessToken(member.getUsername(), member.getRole().name());
    }
    public void logout(String accessToken, String username){// 1. RT 삭제 (완료)
        refreshTokenRepository.deleteById(username);
        String pureToken = accessToken.substring(7);
        long expiration = jwtUtil.getExpiration(pureToken);
        // 2. AT 블랙리스트 등록 (이게 있어야 함!)
        // "Bearer " 떼고 순수 토큰만 저장해!
        redisTemplate.opsForValue().set(pureToken, "logout", expiration, TimeUnit.MILLISECONDS);
        System.out.println("로그아웃했어요 " + username + "님!");
    }
}
