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

    @Enumerated(EnumType.STRING)
    private Role role;

    // 생성자
    @Builder
    public Member(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
}