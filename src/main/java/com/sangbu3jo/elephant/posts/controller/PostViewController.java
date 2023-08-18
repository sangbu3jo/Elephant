package com.sangbu3jo.elephant.posts.controller;

import com.sangbu3jo.elephant.posts.dto.PostResponseDto;
import com.sangbu3jo.elephant.posts.service.PostService;
import com.sangbu3jo.elephant.security.UserDetailsImpl;
import com.sangbu3jo.elephant.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController //나중에 바꿔야 함
@RequiredArgsConstructor
@RequestMapping("/api")
public class PostViewController {

    private final PostService postService;

    //메인페이지에서 섹션을 나눠서 각 카테고리마다 5개 정도 가져오기
//    @GetMapping("/posts/categories")
//    public List<PostResponseDto> getAllPosts() {
//        return postService.getAllPosts();
//
//    }

    //게시글 카테고리 별 전체 조회
    @GetMapping("/posts/categories/{category}/{page_num}")
    public List<PostResponseDto> getCategoryPost(@PathVariable Integer category,
                                                 @PathVariable Integer page_num) {


        return postService.getCategoryPost(category, page_num);

    }

    //게시글 상세 페이지
    @GetMapping("/posts/{post_id}")
    public PostResponseDto getPost(@PathVariable Long post_id,
                                   @AuthenticationPrincipal UserDetailsImpl userDetails){
        return postService.getPost(post_id, userDetails.getUser());
    }
}

//param 방식 이용