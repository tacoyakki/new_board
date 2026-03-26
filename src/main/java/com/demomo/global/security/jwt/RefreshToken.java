package com.demomo.global.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@AllArgsConstructor
@RedisHash(value = "refreshToken", timeToLive = 60 * 60 * 24 * 14) // 14일 유지
public class RefreshToken {
    @Id
    private String username; // Redis의 Key가 됩니다.
    private String refreshToken;
}