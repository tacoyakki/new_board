package com.demomo.board.dto;

import com.demomo.board.domain.Board;
import com.demomo.board.domain.Comment;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;


public record BoardResponse(
        Long id,
        String title,
        String content,
        String writer,
        List<CommentResponse> comments, // 댓글 목록
        @JsonFormat(pattern = "yyyy년 MM월 dd일 HH:mm")
        LocalDateTime createdAt
) {
    // 1. 전체 목록 조회용 생성자 (댓글이 필요 없을 때)
    public BoardResponse(Board board) {
        this(
                board.getId(),
                board.getTitle(),
                board.getContent(),
                board.getMember().getUsername(),
                null, // 목록에서는 댓글을 안 보여줄 거니까 null 또는 빈 리스트
                board.getCreatedAt()
        );
    }

    // 2. 상세 조회용 생성자 (댓글이 필요할 때)
    public BoardResponse(Board board, List<Comment> comments) {
        this(
                board.getId(),
                board.getTitle(),
                board.getContent(),
                board.getMember().getUsername(),
                comments.stream().map(CommentResponse::new).toList(),
                board.getCreatedAt()
        );
    }
}