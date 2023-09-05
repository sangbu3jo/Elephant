package com.sangbu3jo.elephant.posts.dto;

import com.sangbu3jo.elephant.posts.entity.Post;
import com.sangbu3jo.elephant.report.entity.Report;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
public class ReportPostResponseDto {
    private Long id;
    private Long userId;
    private Long oripostId;
    private String title;
    private String content;
    private String category;
    private String username;
    private String nickname;
    private String files;
    private Boolean completed;
    private Integer view_cnt;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private List<PostCommentResponseDto> postCommentList;
    private String reason;

    public ReportPostResponseDto(Report report) {
        this.id = report.getId();
        this.userId = report.getUser().getId();
        this.oripostId = report.getPost().getId();
        this.title = report.getPost().getTitle();
        this.username = report.getUser().getUsername();
        this.nickname = report.getUser().getNickname();
        this.content = report.getPost().getContent();
        this.category = report.getPost().getCategory().getCategoryName();
        this.completed = report.getPost().getCompleted();
        this.files = report.getPost().getFiles();
        this.view_cnt = report.getPost().getViewCnt()==null ? 0 : report.getPost().getViewCnt();
        this.createdAt = report.getCreatedAt();
        this.modifiedAt = report.getModifiedAt();
        this.postCommentList = report.getPost().getCommentList().stream()
                .map(PostCommentResponseDto::new)
                .sorted(Comparator.comparing(PostCommentResponseDto::getCreatedAt))
                .toList();
        this.userId = report.getUser().getId();
        this.reason = report.getReason();
    }

}
