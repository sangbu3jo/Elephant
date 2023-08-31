package com.sangbu3jo.elephant.posts.controller;

import com.sangbu3jo.elephant.posts.dto.PostResponseDto;
import com.sangbu3jo.elephant.posts.entity.Category;
import com.sangbu3jo.elephant.posts.entity.Post;
import com.sangbu3jo.elephant.posts.repository.PostRepository;
import com.sangbu3jo.elephant.posts.service.PostService;
import com.sangbu3jo.elephant.security.UserDetailsImpl;
import com.sangbu3jo.elephant.users.entity.UserRoleEnum;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.patterns.ExactTypePattern;
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

  //게시글 카테고리 별 전체 조회
  //pagination
  @GetMapping("/posts/categories/{category}")
  public String getCategoryPost(
      @PathVariable Integer category,
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @RequestParam(required = false, defaultValue = "0", value = "page") Integer pageNo,
      Pageable pageable,
      Model model) {

    checkAdmin(model,userDetails);
    pageNo = (pageNo == 0) ? 0 : (pageNo - 1);
    Page<PostResponseDto> postResponseDtoList = postService.getCategoryPost(category, pageable,
        pageNo);
    model.addAttribute("categoryName", Category.getCategory(category));
    model.addAttribute("posts", postResponseDtoList);

    return "category";
  }


  //게시글 카테고리 검색 조회
  @GetMapping("/posts/categories/{category}/titles")
  public String getSearchTitle(@PathVariable Integer category,
      @RequestParam("title") String title,
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @RequestParam(required = false, defaultValue = "0", value = "page") Integer pageNo,
      Pageable pageable,
      Model model) {

    checkAdmin(model,userDetails);
    Page<PostResponseDto> postResponseDtoList = postService.getSearchTitle(category, pageable,
        pageNo, title);
    model.addAttribute("categoryName", Category.getCategory(category));
    model.addAttribute("searchedTitle", title);
    model.addAttribute("posts", postResponseDtoList);

    return "searchedPage";
  }


  //게시글 상세(단건 조회) 페이지
  @GetMapping("/posts/{post_id}")
  public String getPost(@PathVariable Long post_id,
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      Model model) {
    PostResponseDto postResponseDto = postService.getPost(post_id, userDetails.getUser());
    postResponseDto.convertContent();
    model.addAttribute("post", postResponseDto);
    model.addAttribute("loginUser", userDetails.getUser().getId());
    checkAdmin(model,userDetails);
    return "post";
  }


  //프로젝트
  @GetMapping("/posts/project")
  public String getProject(Model model) {

    //checkAdmin(model,userDetails);
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
    checkAdmin(model,userDetails);
    return "updatePost";
  }


  private void checkAdmin(Model model, UserDetailsImpl userDetails) {
    if (userDetails != null) {
      Boolean admin = false;
      if (userDetails.getUser().getRole().equals(UserRoleEnum.ADMIN)) {
        admin = true;
      }
      model.addAttribute("admin", admin);
    }
  }
   
}
  
