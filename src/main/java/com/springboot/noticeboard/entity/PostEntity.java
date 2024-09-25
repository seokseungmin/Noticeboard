package com.springboot.noticeboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class PostEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // fetch = FetchType.LAZY: 데이터를 지연 로딩하겠다는 의미입니다.
    // 즉, 게시글 정보를 먼저 가져오고, 작성자 정보는 필요할 때만 데이터베이스에서 가져옵니다.
    // 성능 최적화를 위해 사용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity author;

    //cascade = CascadeType.ALL: 게시글이 삭제되거나 수정될 때, 연관된 댓글들도 함께 영향을 받습니다.
    // 예를 들어, 게시글을 삭제하면 연관된 댓글도 같이 삭제됩니다.
    //orphanRemoval = true: 고아 객체 제거 옵션입니다.
    // 만약 댓글이 리스트에서 제거되면, 데이터베이스에서도 삭제됩니다.
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentEntity> comments;

    @Column(nullable = false)
    private int commentCount = 0;
}