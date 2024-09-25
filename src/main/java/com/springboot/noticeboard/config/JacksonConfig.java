package com.springboot.noticeboard.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
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
        objectMapper.coercionConfigFor(Role.class)
                .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull); // 빈 문자열을 null로 취급
        return objectMapper;
    }
}
