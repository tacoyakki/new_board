package com.demomo.board.dto;

import com.demomo.board.domain.Board;
import java.time.LocalDateTime;

public record BoardResponse(
        Long id,
        String title,
        String content,
        String writer,
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