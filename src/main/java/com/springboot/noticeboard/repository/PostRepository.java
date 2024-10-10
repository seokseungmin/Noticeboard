package com.springboot.noticeboard.repository;

import com.querydsl.core.types.dsl.StringPath;
import com.springboot.noticeboard.entity.PostEntity;
import com.springboot.noticeboard.entity.QPostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

public interface PostRepository extends JpaRepository<PostEntity, Long>, QuerydslPredicateExecutor<PostEntity>, QuerydslBinderCustomizer<QPostEntity> {

    @Override
    default void customize(QuerydslBindings bindings, QPostEntity root) {
        // 기본적으로 모든 문자열 검색은 부분 일치를 허용
        bindings.bind(String.class).first((StringPath path, String value) -> path.containsIgnoreCase(value));
    }
}
