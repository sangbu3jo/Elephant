package com.sangbu3jo.elephant.report.service;

import com.sangbu3jo.elephant.notification.service.NotificationService;
import com.sangbu3jo.elephant.posts.dto.PostResponseDto;
import com.sangbu3jo.elephant.posts.entity.Post;
import com.sangbu3jo.elephant.posts.repository.PostRepository;
import com.sangbu3jo.elephant.report.dto.ReportRequestDto;
import com.sangbu3jo.elephant.report.entity.Report;
import com.sangbu3jo.elephant.report.repository.ReportRepository;
import com.sangbu3jo.elephant.users.entity.User;
import com.sangbu3jo.elephant.users.entity.UserRoleEnum;
import com.sangbu3jo.elephant.users.repository.UserRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final NotificationService notificationService;

    // 신고당한 게시글을 DB에 저장
    public String reportPost(User requestuser, Long postId, ReportRequestDto reportRequestDto) {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 게시글 입니다.")
        );

        User user = userRepository.findById(requestuser.getId()).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 사용자 입니다.")
        );

        Report report = new Report(post, user, reportRequestDto.getReason());
        reportRepository.save(report);

        // 알림 정보 (어드민 유저들에게 전달)
        List<User> users = userRepository.findAll();
        for(User adminUser : users) {
            String notificationContent = user.getNickname() + "님이 \"" + post.getTitle() + "\" 게시글을 신고하였습니다. 사유 : " + reportRequestDto.getReason();
            String notificationUrl = "/posts/" + post.getId(); // 게시글로 이동할 수 있는 URL

            if(adminUser.getRole().equals(UserRoleEnum.ADMIN)) {
                notificationService.reportPostNotification(adminUser.getId(), post.getId(), notificationContent, notificationUrl);
            }
        }

        return "게시글이 신고되었습니다.";
    }

    // 신고당한 게시글 조회
    public List<PostResponseDto> getReportPosts(User requestuser) {
        if(!requestuser.getRole().equals(UserRoleEnum.ADMIN)){
            throw new IllegalArgumentException("관리자 기능입니다.");
        }

        List<Report> reportedPosts = reportRepository.findAll();
        List<PostResponseDto> postResponseDtos = new ArrayList<>();

        for(Report report : reportedPosts){
            Post post = report.getPost();
            PostResponseDto postResponseDto = new PostResponseDto(post);
            postResponseDtos.add(postResponseDto);
        }

        return postResponseDtos;
    }
}
