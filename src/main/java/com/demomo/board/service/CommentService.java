package com.demomo.board.service;

import com.demomo.board.domain.Board;
import com.demomo.board.domain.Comment;
import com.demomo.board.dto.CommentRequest;
import com.demomo.board.repository.BoardRepository;
import com.demomo.board.repository.CommentRepository;
import com.demomo.member.domain.Member;
import com.demomo.member.domain.Role;
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
    @Transactional
    public Long updateComment(Long id, CommentRequest request, String username) {
        // 1. 댓글이 있는지 확인
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 댓글이 존재하지 않습니다."));

        // 2. 수정을 요청한 유저 정보 가져오기
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        // 3. 권한 체크: 작성자 본인도 아니고 관리자도 아니면 에러!
        if (!comment.getMember().getUsername().equals(username) && member.getRole() != Role.ADMIN) {
            throw new RuntimeException("수정 권한이 없습니다.");
        }

        // 4. 내용 업데이트 (Dirty Checking으로 자동 반영됨)
        comment.update(request.content());

        return comment.getId();
    }

    @Transactional
    public void deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글이 없습니다."));

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        if (!comment.getMember().getUsername().equals(username) && member.getRole() != Role.ADMIN) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }
}