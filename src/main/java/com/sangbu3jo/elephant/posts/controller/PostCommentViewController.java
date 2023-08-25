package com.sangbu3jo.elephant.posts.controller;

import com.sangbu3jo.elephant.posts.dto.PostCommentResponseDto;
import com.sangbu3jo.elephant.posts.service.PostCommentService;
import com.sangbu3jo.elephant.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class PostCommentViewController {


    private final PostCommentService postCommentService;

//    //댓글 단건 조회
//    @GetMapping("/posts/comment/{comment_id}")
//    public String getComment(@PathVariable Long comment_id,
//                             @RequestBody UserDetailsImpl userDetails,
//                             Model model){
//
//        PostCommentResponseDto postCommentResponseDto = postCommentService.getComment(comment_id, userDetails.getUser());
//        model.addAttribute("post", postCommentResponseDto);
//
//        return "post";
//    }
}
