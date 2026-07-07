package com.demomo.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BoardRequest(
        @NotBlank(message = "제목을 입력해 주세요.")
        @Size(max = 80, message = "제목은 80자 이하여야 합니다.")
        String title,
        @NotBlank(message = "내용을 입력해 주세요.")
        @Size(max = 3000, message = "내용은 3000자 이하여야 합니다.")
        String content
) {}
