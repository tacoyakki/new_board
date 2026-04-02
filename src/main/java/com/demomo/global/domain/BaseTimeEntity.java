package com.demomo.global.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass // ⭐ 이 클래스를 상속받으면 필드(컬럼)를 자식에게 물려줌
@EntityListeners(AuditingEntityListener.class) // ⭐ 자동으로 시간을 기록해주는 리스너
public abstract class BaseTimeEntity {

    @CreatedDate // 생성 시 자동 기록
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate // 수정 시 자동 기록
    private LocalDateTime updatedAt;
}