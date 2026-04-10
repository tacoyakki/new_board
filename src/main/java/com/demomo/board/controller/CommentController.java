package com.demomo.board.controller;

import com.demomo.board.dto.CommentRequest;
import com.demomo.board.service.CommentService;
import com.demomo.board.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

// CommentController.java

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments") // 공통 주소
public class CommentController {

    private final CommentService commentService;

    // 1. 댓글 수정 API
    @PutMapping("/{id}")
    public ResponseEntity<Long> updateComment(
            @PathVariable Long id,
            @RequestBody CommentRequest request, // 댓글 내용이 담긴 DTO
            @AuthenticationPrincipal UserDetails userDetails) { // 현재 로그인한 사람

        Long updatedId = commentService.updateComment(id, request, userDetails.getUsername());
        return ResponseEntity.ok(updatedId);
    }

    // 2. 댓글 삭제 API
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        commentService.deleteComment(id, userDetails.getUsername());
        return ResponseEntity.noContent().build(); //204
    }
}