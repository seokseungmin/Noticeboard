package com.springboot.noticeboard.entity;

import com.springboot.noticeboard.type.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "username", "email", "role"})
@Entity
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostEntity> posts = new ArrayList<>();  // 초기화 필수

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CommentEntity> comments = new ArrayList<>();  // 초기화 필수

    // 연관관계 편의 메서드 - Post 추가
    public void addPost(PostEntity post) {
        posts.add(post);
        post.setAuthor(this);
    }

    // 연관관계 편의 메서드 - Post 제거
    public void removePost(PostEntity post) {
        posts.remove(post);
        post.setAuthor(null);
    }

    // 연관관계 편의 메서드 - Comment 추가
    public void addComment(CommentEntity comment) {
        comments.add(comment);
        comment.setAuthor(this);
    }

    // 연관관계 편의 메서드 - Comment 제거
    public void removeComment(CommentEntity comment) {
        comments.remove(comment);
        comment.setAuthor(null);
    }
}

