package com.springboot.noticeboard.controller;

import com.springboot.noticeboard.entity.PostEntity;
import com.springboot.noticeboard.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
//
@RestController
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {

    private final PostService postService;

    //SecurityConfig에 권한 없어도 접근할수 있게 해놓았는데도 403 에러가 계속뜸
    // 게시글 목록 조회
    @GetMapping("/getPosts")
    public ResponseEntity<?> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createDate") String sortBy,  // createDate로 변경
            @RequestParam(defaultValue = "desc") String direction) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        Page<PostEntity> postPage = postService.getPosts(pageable);

        return ResponseEntity.ok(postPage.map(post -> Map.of(
                "id", post.getId(),
                "title", post.getTitle(),
                "createDate", post.getCreateDate(),  // 이 부분도 createDate로 수정
                "commentCount", post.getCommentCount()
        )));
    }


    // 특정 게시글 조회
    @GetMapping("/getPost/{postId}")
    public ResponseEntity<?> getPost(@PathVariable Long postId) {
        PostEntity post = postService.getPost(postId);
        return ResponseEntity.ok(Map.of(
                "title", post.getTitle(),
                "content", post.getContent(),
                "author", post.getAuthor().getUsername(),
                "createDate", post.getCreateDate()
        ));
    }

}
