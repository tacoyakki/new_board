package com.demomo.board.controller;

import com.demomo.board.dto.BoardRequest;
import com.demomo.board.dto.BoardResponse;
import com.demomo.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardService boardService;

    // 1. 게시글 생성
    @PostMapping
    public ResponseEntity<Long> createBoard(
            @RequestBody BoardRequest request, // BoardRequest로 이름 변경
            @AuthenticationPrincipal String username) {

        // record는 getter가 아니라 필드명()으로 호출!
        Long boardId = boardService.create(request.title(), request.content(), username);
        return ResponseEntity.ok(boardId);
    }

    // 2. 게시글 전체 조회
    @GetMapping
    public ResponseEntity<List<BoardResponse>> getAllBoards() {
        return ResponseEntity.ok(boardService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardResponse> getBoard(@PathVariable Long id) {
        return ResponseEntity.ok(boardService.findById(id));
    }

    // 4. 수정
    @PutMapping("/{id}")
    public ResponseEntity<Long> updateBoard(
            @PathVariable Long id,
            @RequestBody BoardRequest request,
            @AuthenticationPrincipal String username) {

        return ResponseEntity.ok(boardService.update(id, request, username));
    }

    // 5. 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(
            @PathVariable Long id,
            @AuthenticationPrincipal String username) {

        boardService.delete(id, username);
        return ResponseEntity.ok().build();
    }
}