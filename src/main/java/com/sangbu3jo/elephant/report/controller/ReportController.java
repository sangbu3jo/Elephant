package com.sangbu3jo.elephant.report.controller;


import com.sangbu3jo.elephant.posts.dto.PostResponseDto;
import com.sangbu3jo.elephant.posts.entity.Post;
import com.sangbu3jo.elephant.report.dto.ReportRequestDto;
import com.sangbu3jo.elephant.report.service.ReportService;
import com.sangbu3jo.elephant.security.UserDetailsImpl;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReportController {
    private final ReportService reportService;

    // 신고당한 게시글을 DB에 저장
    @PostMapping("/posts/report/{postId}")
    public ResponseEntity<String> reportPost(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                             @PathVariable Long postId,
                                             @RequestBody ReportRequestDto reportRequestDto){
        String result = reportService.reportPost(userDetails.getUser(), postId, reportRequestDto);
        return ResponseEntity.ok(result);
    }

    // 신고당한 게시글 조회
    @GetMapping("/posts/report")
    public ResponseEntity<List<PostResponseDto>> getReportPosts(@AuthenticationPrincipal UserDetailsImpl userDetails){
        List<PostResponseDto> posts = reportService.getReportPosts(userDetails.getUser());
        return ResponseEntity.ok(posts);
    }


}
