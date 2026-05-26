package com.example.dual_tales.global.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass //해당 클래스를 상속받는 자식클래스들에게 필드를 물려줌
@EntityListeners(AuditingEntityListener.class) //자동 시간 기록 리스너
public abstract class BaseTimeEntity {
    @CreatedDate //엔티티 생성되어 저장될 때 시간 저장
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate //조회한 엔티티 값을 변경할 때 시간 자동 저장
    private LocalDateTime updatedAt;
}
