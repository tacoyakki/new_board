package com.demomo.member.repository;

import com.demomo.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository  extends JpaRepository<Member,Long> {
    Optional<Member> findByUsername(String username);
    boolean existsByUsername(String username);
}

