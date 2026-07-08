package com.demomo.member.service;

import com.demomo.member.domain.Member;
import com.demomo.member.dto.LoginRequest;
import com.demomo.member.repository.MemberRepository;
import com.demomo.member.dto.SignupRequest;
import com.demomo.global.security.jwt.JwtUtil;
import com.demomo.global.security.jwt.RefreshToken;
import com.demomo.global.security.jwt.RefreshTokenRepository;
import com.demomo.global.exception.ApiException;
import com.demomo.member.dto.AuthResponse;
import com.demomo.member.dto.ProfileResponse;
import com.demomo.member.dto.UpdateProfileRequest;
import com.demomo.board.repository.BoardRepository;
import com.demomo.board.repository.CommentRepository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

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
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final ProfileImageService profileImageService;


    public AuthResponse login(LoginRequest request) {
        Member member = memberRepository.findByUsername(request.username())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."));

        if (member.getPassword() == null || !passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        // 1. Access Token 발급
        String accessToken = jwtUtil.createAccessToken(member.getUsername(), member.getRole().name());

        // 2. Refresh Token 발급
        String refreshToken = jwtUtil.createRefreshToken(member.getUsername());

        // 3. Redis에 저장 (이미 있으면 덮어쓰기 됨)
        refreshTokenRepository.save(new RefreshToken(member.getUsername(), refreshToken));

        return new AuthResponse(accessToken, refreshToken);
    }

    public void signup(SignupRequest request) {

        if (memberRepository.existsByUsername(request.username())) {
            throw new ApiException(HttpStatus.CONFLICT, "이미 존재하는 사용자입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        Member member = Member.builder()
                .username(request.username())
                .password(encodedPassword)
                .role(com.demomo.member.domain.Role.USER)
                .build();

        memberRepository.save(member);
    }


    public Member findByUsername(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String username) {
        return new ProfileResponse(findByUsername(username));
    }

    @Transactional
    public ProfileResponse updateProfile(String username, UpdateProfileRequest request) {
        if (request.nickname() != null && request.nickname().trim().length() > 30) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "닉네임은 30자 이하로 입력해 주세요.");
        }
        if (request.bio() != null && request.bio().trim().length() > 300) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "소개는 300자 이하로 입력해 주세요.");
        }
        if (request.profileImageUrl() != null && request.profileImageUrl().trim().length() > 1000) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "프로필 이미지 주소가 너무 깁니다.");
        }
        Member member = findByUsername(username);
        member.updateProfile(request.nickname(), request.bio(), request.profileImageUrl());
        return new ProfileResponse(member);
    }

    public AuthResponse reissue(String refreshToken) {
        // 1. 리프레시 토큰 자체가 유효한지 확인 (만료 여부 등)
        if (!jwtUtil.isValid(refreshToken)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었거나 유효하지 않습니다.");
        }

        // 2. 토큰에서 유저 이름 추출
        String username = jwtUtil.getUsernameFromRefreshToken(refreshToken);

        // 3. Redis에 저장된 토큰 꺼내기
        RefreshToken savedToken = refreshTokenRepository.findById(username)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "로그인 정보가 없습니다. 다시 로그인해 주세요."));

        // 4. 사용자가 보낸 토큰과 Redis의 토큰이 일치하는지 대조
        if (!savedToken.getRefreshToken().equals(refreshToken)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다.");
        }

        // 5. 일치한다면 새로운 Access Token 발급
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        String accessToken = jwtUtil.createAccessToken(member.getUsername(), member.getRole().name());
        return new AuthResponse(accessToken, refreshToken);
    }
    public void logout(String accessToken, String username){// 1. RT 삭제 (완료)
        refreshTokenRepository.deleteById(username);
        if (accessToken == null || !accessToken.startsWith("Bearer ")) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "올바른 인증 토큰이 필요합니다.");
        }
        String pureToken = accessToken.substring(7);
        long expiration = jwtUtil.getExpiration(pureToken);
        // 2. AT 블랙리스트 등록 (이게 있어야 함!)
        // "Bearer " 떼고 순수 토큰만 저장해!
        if (expiration > 0) {
            redisTemplate.opsForValue().set(pureToken, "logout", expiration, TimeUnit.MILLISECONDS);
        }
    }

    @Transactional
    public void withdraw(String accessToken, String username) {
        Member member = findByUsername(username);
        commentRepository.deleteAllByMemberId(member.getId());
        commentRepository.deleteAllByBoardMemberId(member.getId());
        boardRepository.deleteAllByMemberId(member.getId());
        memberRepository.delete(member);
        memberRepository.flush();

        profileImageService.delete(member.getProfileImageUrl());
        clearTokensAfterWithdrawal(accessToken, username);
    }

    private void clearTokensAfterWithdrawal(String accessToken, String username) {
        try {
            refreshTokenRepository.deleteById(username);
            if (accessToken != null && accessToken.startsWith("Bearer ")) {
                String token = accessToken.substring(7);
                long expiration = jwtUtil.getExpiration(token);
                if (expiration > 0) {
                    redisTemplate.opsForValue().set(token, "withdrawn", expiration, TimeUnit.MILLISECONDS);
                }
            }
        } catch (RuntimeException ignored) {
            // DB 회원 삭제 후에는 기존 토큰으로 회원 기능을 사용할 수 없습니다.
        }
    }
}
