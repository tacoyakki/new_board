package com.demomo.board.dto;

import com.demomo.board.domain.Board;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record BoardResponse(
        Long id,
        String title,
        String content,
        String writer,

        @JsonFormat(pattern = "yyyy년 MM월 dd일 HH시 mm분 ss초")
        LocalDateTime createdAt
) {
    public BoardResponse(Board board) {
        this(
                board.getId(),
                board.getTitle(),
                board.getContent(),
                board.getMember().getUsername(),
                board.getCreatedAt()
        );
    }
}