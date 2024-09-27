package com.springboot.noticeboard.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.springboot.noticeboard.type.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
v.HttpMessageNotReadableException: JSON parse error: Cannot coerce empty String ("") to com.springboot.noticeboard.type.Role value (but could if coercion was enabled using CoercionConfig)]
 오류 발생해서 추가한 Config클래스
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 빈 문자열을 null로 처리하는 설정
        objectMapper.coercionConfigFor(Role.class)
                .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);

        // Java 8 날짜 및 시간 관련 모듈 등록
        objectMapper.registerModule(new JavaTimeModule());

        // LocalDateTime을 타임스탬프 대신 ISO-8601 포맷으로 직렬화하도록 설정
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return objectMapper;
    }
}
