package com.demomo.board.controller;

import com.demomo.board.dto.CommentRequest;
import com.demomo.board.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    // 1. 댓글 수정 API
    @PutMapping("/{id}")
    public ResponseEntity<Long> updateComment(
            @PathVariable Long id,
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal String username) { // 🟢 UserDetails 대신 String 으로 변경!

        Long updatedId = commentService.updateComment(id, request, username); // 🟢 바로 username 사용
        return ResponseEntity.ok(updatedId);
    }

    // 2. 댓글 삭제 API
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            @AuthenticationPrincipal String username) { // 🟢 String 으로 변경!

        commentService.deleteComment(id, username); // 🟢 바로 username 사용
        return ResponseEntity.noContent().build();
    }

    // 3. 댓글 작성 API
    @PostMapping("/{boardId}")
    public ResponseEntity<Long> createComment(
            @PathVariable Long boardId,
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal String username) { // 🟢 String 으로 변경!

        Long commentId = commentService.create(boardId, request, username); // 🟢 바로 username 사용
        return ResponseEntity.ok(commentId);
    }
}