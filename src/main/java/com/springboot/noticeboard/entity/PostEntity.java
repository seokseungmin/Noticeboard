package com.springboot.noticeboard.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "title", "content", "commentCount"})
@Entity
public class PostEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity author;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CommentEntity> comments = new ArrayList<>();  // 초기화 필수

    @Column(nullable = false)
    @Builder.Default
    private int commentCount = 0;

    // 연관관계 편의 메서드 - Comment 추가
    public void addComment(CommentEntity comment) {
        comments.add(comment);
        comment.setPost(this);
    }

    // 연관관계 편의 메서드 - Comment 제거
    public void removeComment(CommentEntity comment) {
        comments.remove(comment);
        comment.setPost(null);
    }

    // 연관관계 편의 메서드 - Author 설정
    public void setAuthor(UserEntity author) {
        if (this.author != null) {
            this.author.getPosts().remove(this);  // 기존 관계 제거
        }
        this.author = author;
        if (author != null) {
            author.getPosts().add(this);  // 새로운 관계 설정
        }
    }
}
