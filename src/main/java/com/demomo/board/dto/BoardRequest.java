package com.demomo.board.dto;

// 제목과 내용만 담는 아주 심플한 레코드
public record BoardRequest(
        String title,
        String content
) {}