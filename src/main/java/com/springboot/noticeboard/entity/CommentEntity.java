package com.springboot.noticeboard.entity;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "content"})
@Entity
public class CommentEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private PostEntity post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity author;

    // 연관관계 편의 메서드 - Post와 연관된 메서드
    public void setPost(PostEntity post) {
        if (this.post != null) {
            this.post.getComments().remove(this);  // 기존 관계 제거
        }
        this.post = post;
        if (post != null) {
            post.getComments().add(this);  // 새로운 관계 설정
        }
    }

    // 연관관계 편의 메서드 - Author(User)와 연관된 메서드
    public void setAuthor(UserEntity author) {
        if (this.author != null) {
            this.author.getComments().remove(this);  // 기존 관계 제거
        }
        this.author = author;
        if (author != null) {
            author.getComments().add(this);  // 새로운 관계 설정
        }
    }
}
