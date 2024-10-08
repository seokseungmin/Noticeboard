package com.springboot.noticeboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@MappedSuperclass
public abstract class AuditableEntity extends BaseEntity {

    @LastModifiedDate  // 엔티티가 수정될 때 자동으로 날짜 설정
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;
}
