package com.springboot.noticeboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing  // Auditing 기능 활성화
public class NoticeboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(NoticeboardApplication.class, args);
    }

}
