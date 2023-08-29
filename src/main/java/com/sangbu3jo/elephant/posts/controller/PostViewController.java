package com.sangbu3jo.elephant.posts.controller;

import com.sangbu3jo.elephant.posts.dto.PostResponseDto;
import com.sangbu3jo.elephant.posts.service.PostService;
import com.sangbu3jo.elephant.security.UserDetailsImpl;
import com.sangbu3jo.elephant.users.entity.UserRoleEnum;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class PostViewController {

    //RequestParam 방식 이용 프론트에서 값을 받아올 때
    private final PostService postService;

//    //메인페이지에서 섹션을 나눠서 각 카테고리마다 5개 정도 가져오기
//    @GetMapping("/posts/categories")
//    public String getAllPosts(Model model) {
//
//        List<PostResponseDto> postResponseDtoList = postService.getAllPosts();
//
//        model.addAttribute("mainPage", postResponseDtoList);
//
//        return "mainPage";
//
//    }

    //게시글 카테고리 별 전체 조회
    //pagination
    @GetMapping("/posts/categories/{category}")
    public String getCategoryPost(
            @PathVariable Integer category,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(required = false, defaultValue = "0", value = "page") Integer pageNo,
            Pageable pageable,
            Model model) {

        Boolean admin = false;
        if (userDetails.getUser().getRole().equals(UserRoleEnum.ADMIN)) {
            admin = true;
        }

        pageNo = (pageNo == 0) ? 0 : (pageNo - 1);
        Page<PostResponseDto> postResponseDtoList = postService.getCategoryPost(category, pageable, pageNo);
        model.addAttribute("categoryName", postResponseDtoList.getContent().get(0).getCategory());
        model.addAttribute("posts", postResponseDtoList);

        return "category";
    }


    //게시글 카테고리 검색 조회
    //슬라이스 구현
    @GetMapping("/posts/categories/{category}/titles")
    public String getSearchTitle(@PathVariable Integer category,
                                 @RequestParam("title") String title,
                                 @AuthenticationPrincipal UserDetailsImpl userDetails,
                                 Model model) {
        Boolean admin = false;
        if (userDetails.getUser().getRole().equals(UserRoleEnum.ADMIN)) {
            admin = true;
        }

        List<PostResponseDto> postResponseDtoList = postService.getSearchTitle(category, title);

        model.addAttribute("searchedPage", postResponseDtoList);

        return "searchedPage";
    }
    //게시글 상세(단건 조회) 페이지
    @GetMapping("/posts/{post_id}")
    public String getPost(@PathVariable Long post_id,
                          @AuthenticationPrincipal UserDetailsImpl userDetails,
                          Model model) {
        PostResponseDto postResponseDto = postService.getPost(post_id, userDetails.getUser());
        model.addAttribute("post", postResponseDto);
        return "post";
    }



    //프로젝트
    @GetMapping("/posts/project")
    public String getProject(Model model){

        List<PostResponseDto> projResponseDtoList = postService.getProject();
        List<PostResponseDto> stuResponseDtoList = postService.getStudy();
        List<PostResponseDto> exaResponseDtoList = postService.getExam();
        List<PostResponseDto> foruResponseDtoList = postService.getForum();
        model.addAttribute("project", projResponseDtoList);
        model.addAttribute("study", stuResponseDtoList);
        model.addAttribute("exam", exaResponseDtoList);
        model.addAttribute("forum", foruResponseDtoList);

        return "mainPage";
    }


//    //스터디
//    @GetMapping("/posts/study")
//    public String getStudy(Model model){
//
//        Slice<PostResponseDto> postResponseDtoList = postService.getStudy();
//        model.addAttribute("study", postResponseDtoList);
//        return "mainPage";
//    }
//
//    //문제은행
//    @GetMapping("/posts/exam")
//    public String getExam(Model model){
//
//        List<PostResponseDto> postResponseDtoList = postService.getExam();
//        model.addAttribute("exam", postResponseDtoList);
//        return "mainPage";
//    }

    //게시글 생성 페이지로 이동
    @GetMapping("/post-page")
    public String createPost() {
        return "createPost";
    }

    //게시글 수정 페이지로 이동
    @GetMapping("/posts/update/{post_id}")
    public String updatePost(@PathVariable Long post_id,
                             @AuthenticationPrincipal UserDetailsImpl userDetails,
                             Model model) {

        PostResponseDto postResponseDto = postService.getPost(post_id, userDetails.getUser());
        model.addAttribute("updatePost", postResponseDto);
        return "updatePost";
    }
}

