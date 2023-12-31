package com.sangbu3jo.elephant.common;


import com.sangbu3jo.elephant.posts.dto.PostResponseDto;
import com.sangbu3jo.elephant.posts.service.PostService;
import com.sangbu3jo.elephant.security.UserDetailsImpl;
import com.sangbu3jo.elephant.users.entity.UserRoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeViewController {

    private final PostService postService;

    // 메인 페이지 (index / mainPage)
    @GetMapping("/")
    public String getMain(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails){
        if (userDetails != null) {
            checkAdmin(model,userDetails);
            List<PostResponseDto> projResponseDtoList = postService.getProject();
            List<PostResponseDto> stuResponseDtoList = postService.getStudy();
            List<PostResponseDto> exaResponseDtoList = postService.getExam();
            List<PostResponseDto> foruResponseDtoList = postService.getForum();
            model.addAttribute("projects", projResponseDtoList);
            model.addAttribute("studies", stuResponseDtoList);
            model.addAttribute("exams", exaResponseDtoList);
            model.addAttribute("forums", foruResponseDtoList);

            return "mainPage";
        }
        return "index";
    }


    //프로젝트
    @GetMapping("/main")
    public String getProject(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails){
        if (userDetails != null) {
            checkAdmin(model, userDetails);
        }
        List<PostResponseDto> projResponseDtoList = postService.getProject();
        List<PostResponseDto> stuResponseDtoList = postService.getStudy();
        List<PostResponseDto> exaResponseDtoList = postService.getExam();
        List<PostResponseDto> foruResponseDtoList = postService.getForum();
        model.addAttribute("projects", projResponseDtoList);
        model.addAttribute("studies", stuResponseDtoList);
        model.addAttribute("exams", exaResponseDtoList);
        model.addAttribute("forums", foruResponseDtoList);

        return "mainPage";
    }

    private void checkAdmin(Model model, UserDetailsImpl userDetails) {
        Boolean admin = false;
        if (userDetails.getUser().getRole().equals(UserRoleEnum.ADMIN)) {
            admin = true;
        }
        model.addAttribute("admin", admin);
    }

}
