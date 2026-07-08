package com.demomo.member.domain;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String password;

    private String oauthProvider;

    private String oauthProviderId;

    @Column(length = 30)
    private String nickname;

    @Column(length = 300)
    private String bio;

    @Column(length = 1000)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    private Role role;

    // 생성자
    @Builder
    public Member(String username, String password, Role role, String oauthProvider, String oauthProviderId,
                  String nickname, String bio, String profileImageUrl) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.oauthProvider = oauthProvider;
        this.oauthProviderId = oauthProviderId;
        this.nickname = nickname;
        this.bio = bio;
        this.profileImageUrl = profileImageUrl;
    }

    public String getDisplayName() {
        return nickname == null || nickname.isBlank() ? username : nickname;
    }

    public void updateProfile(String nickname, String bio, String profileImageUrl) {
        this.nickname = nickname == null || nickname.isBlank() ? null : nickname.trim();
        this.bio = bio == null || bio.isBlank() ? null : bio.trim();
        this.profileImageUrl = profileImageUrl == null || profileImageUrl.isBlank() ? null : profileImageUrl.trim();
    }
}
