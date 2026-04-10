package com.demomo.board.service;

import com.demomo.board.domain.Board;
import com.demomo.board.dto.BoardRequest;
import com.demomo.board.repository.BoardRepository;
import com.demomo.member.domain.Member;
import com.demomo.member.domain.Role;
import com.demomo.member.repository.MemberRepository;
import com.demomo.board.dto.BoardResponse;
import com.demomo.board.repository.CommentRepository;
import com.demomo.board.domain.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용으로 설정 (성능 이점)
public class BoardService {

    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;

    @Transactional // 쓰기 작업이므로 별도로 선언 (All or Nothing 보장)
    public Long create(String title, String content, String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        Board board = Board.builder()
                .title(title)
                .content(content)
                .member(member)
                .build();

        return boardRepository.save(board).getId();
    }
    @Transactional(readOnly = true)
    public List<BoardResponse> findAll() {
        // 레포지토리에서 fetch join을 쓴 메서드를 호출하세요!
        return boardRepository.findAllWithMember().stream()
                .map(BoardResponse::new) // BoardResponse(Board board) 생성자 사용
                .toList();
    }

    public BoardResponse findById(Long id) {
        // 1. 게시글 찾기
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 2. (추가) 해당 게시글의 댓글들 다 가져오기
        List<Comment> comments = commentRepository.findAllByBoardId(id);

        // 3. (수정) 게시글과 댓글 목록을 함께 담아서 반환!
        // 어제 수정한 BoardResponse(Board board, List<Comment> comments) 생성자가 쓰여!
        return new BoardResponse(board, comments);
    }

// BoardService.java

    @Transactional
    public Long update(Long id, BoardRequest request, String username) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 유저 정보 조회
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        // ⭐ 본인도 아니고 관리자도 아니면 에러!
        if (!board.getMember().getUsername().equals(username) && member.getRole() != Role.ADMIN) {
            throw new RuntimeException("본인 또는 관리자만 수정할 수 있습니다.");
        }

        board.update(request.title(), request.content());
        return board.getId();
    }

    @Transactional
    public void delete(Long id, String username) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

        // ⭐ 본인도 아니고 관리자도 아니면 에러!
        if (!board.getMember().getUsername().equals(username) && member.getRole() != Role.ADMIN) {
            throw new RuntimeException("본인 또는 관리자만 삭제할 수 있습니다.");
        }

        boardRepository.deleteById(id);
    }
}