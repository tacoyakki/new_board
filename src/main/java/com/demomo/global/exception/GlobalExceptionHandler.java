package com.demomo.global.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Map<String, String>> handleException(RuntimeException e) {
        Map<String, String> body = new HashMap<>();

        if(e.getMessage().contains("본인")){
            body.put("message", e.getMessage());
            return  ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
        }

        body.put("message", "서버 내부 에러가 발생했습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
