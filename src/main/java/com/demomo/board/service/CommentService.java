package com.demomo.board.service;

import com.demomo.board.domain.Board;
import com.demomo.board.domain.Comment;
import com.demomo.board.dto.CommentRequest;
import com.demomo.board.repository.BoardRepository;
import com.demomo.board.repository.CommentRepository;
import com.demomo.member.domain.Member;
import com.demomo.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long create(Long boardId, CommentRequest request, String username) {
        // 1. 해당 게시글이 있는지 확인
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 2. 작성자가 존재하는지 확인
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        // 3. 댓글 생성 (Builder 패턴 사용)
        Comment comment = Comment.builder()
                .content(request.content())
                .board(board)
                .member(member)
                .build();

        // 4. 저장 후 ID 반환
        return commentRepository.save(comment).getId();
    }
}