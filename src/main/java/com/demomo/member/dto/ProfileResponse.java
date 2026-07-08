package com.demomo.member.dto;

import com.demomo.member.domain.Member;

public record ProfileResponse(
        String username,
        String nickname,
        String bio,
        String profileImageUrl,
        boolean nicknameConfigured
) {
    public ProfileResponse(Member member) {
        this(member.getUsername(), member.getDisplayName(), member.getBio(), member.getProfileImageUrl(),
                member.getNickname() != null && !member.getNickname().isBlank());
    }
}
