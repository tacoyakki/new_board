package com.demomo.board.controller;

import com.demomo.board.dto.CommentRequest;
import com.demomo.board.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards/{boardId}/comments") // RESTful한 경로 설계!
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<Long> createComment(
            @PathVariable Long boardId,
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal String username) { // SecurityContext에서 유저정보 가져옴

        Long commentId = commentService.create(boardId, request, username);
        return ResponseEntity.ok(commentId);
    }
}