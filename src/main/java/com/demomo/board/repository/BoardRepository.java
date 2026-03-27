package com.demomo.board.repository;

import com.demomo.board.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {
    // 특정 회원이 쓴 글만 모아보기 기능도 나중에 추가 가능
    List<Board> findByMemberId(Long memberId);
    @Query("select b from Board b join fetch b.member")
    List<Board> findAllWithMember();
}