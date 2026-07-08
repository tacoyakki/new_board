package com.demomo.member.dto;

public record UpdateProfileRequest(
        String nickname,
        String bio,
        String profileImageUrl
) {}
