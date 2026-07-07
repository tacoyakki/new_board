package com.demomo.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequest(
        @NotBlank(message = "댓글 내용을 입력해 주세요.")
        @Size(max = 500, message = "댓글은 500자 이하여야 합니다.")
        String content
) {}
