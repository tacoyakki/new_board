package com.demomo.board.dto;

import com.demomo.board.domain.Comment;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String content,
        String writer,
        @JsonFormat(pattern = "yyyy년 MM월 dd일 HH:mm")
        LocalDateTime createdAt
) {
    // 엔티티를 DTO로 변환해주는 생성자
    public CommentResponse(Comment comment) {
        this(
                comment.getId(),
                comment.getContent(),
                comment.getMember().getUsername(),
                comment.getCreatedAt()
        );
    }
}