package com.sangbu3jo.elephant.posts.controller;

import com.sangbu3jo.elephant.posts.dto.PostRequestDto;
import com.sangbu3jo.elephant.posts.entity.Category;
import com.sangbu3jo.elephant.posts.entity.Post;
import com.sangbu3jo.elephant.posts.service.S3Service;
import com.sangbu3jo.elephant.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;



//
//    @ResponseBody   // Long 타입을 리턴하고 싶은 경우 붙여야 함 (Long - 객체)
//    @PostMapping(value="/api/posts/newFile",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public String saveFile(HttpServletRequest request, @RequestParam(value= "files") MultipartFile files, Post post) throws  IOException {
////        Long postId = s3Service.keepPost(files, post);
//        return s3Service.keepPost(files, post);
//    }

//    @ResponseBody   // Long 타입을 리턴하고 싶은 경우 붙여야 함 (Long - 객체)
//    @PostMapping(value="/api/posts/newFile",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public Long saveFile(HttpServletRequest request, @RequestParam(value= "image") MultipartFile image,
//                         @ModelAttribute Post post) throws  IOException {
//        // 파일 업로드를 통해 게시물을 저장하고 게시물 ID를 반환
//        Long postId = s3Service.keepPost(image, post);
//        return postId;
//    }

//    @ResponseBody   // Long 타입을 리턴하고 싶은 경우 붙여야 함 (Long - 객체)
//    @PostMapping( "/api/posts/newFile")
//    public Long saveFile(@RequestPart(value = "image") MultipartFile image,
//                         @AuthenticationPrincipal UserDetailsImpl userDetails,
//                         @RequestPart PostRequestDto postRequestDto) throws IOException {
//        // 파일 업로드를 통해 게시물을 저장하고 게시물 ID를 반환
//        Long postId = s3Service.keepPost(image, userDetails.getUser(), postRequestDto);
//        return postId;
//    }

    @ResponseBody   // Long 타입을 리턴하고 싶은 경우 붙여야 함 (Long - 객체)
    @PostMapping(value = "/api/posts/new_file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Long saveFile(
            @RequestParam(value = "image") MultipartFile image,
            @RequestParam(value = "category") Category category,
            @Valid @ModelAttribute PostRequestDto postRequestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {


        // 파일 업로드를 통해 게시물을 저장하고 게시물 ID를 반환
        Long postId = s3Service.keepPost(image, postRequestDto, category, userDetails.getUser());

        return postId;
    }

    @PutMapping(value = "/api/posts/new_file/{post_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateFile(@RequestParam(value = "image") MultipartFile image,
                                             @RequestParam(value = "category") Category category,
                                             @Valid @ModelAttribute PostRequestDto postRequestDto,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails,
                                             @PathVariable Long post_id) throws IOException {

        Post post = s3Service.findById(post_id);

        if (userDetails.getUser().getId().equals(post.getUser().getId())) {
            s3Service.modifiedPost(image, category, postRequestDto, post);
            return ResponseEntity.status(200).body("수정 성공");

        } else return ResponseEntity.status(400).body("수정 실패");

    }

    @DeleteMapping("/api/posts/{post_id}/file")
    public ResponseEntity<String> deleteFile(@RequestParam("fileUrl") String fileUrl,
                                             @PathVariable Long post_id,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails) throws IOException {
        Post post = s3Service.findById(post_id);


        if (userDetails.getUser().getId().equals(post.getUser().getId())) {
            s3Service.deleteFile(fileUrl, post);
            return ResponseEntity.status(200).body("이미지가 삭제 되었습니다.");
        } else {
            return ResponseEntity.status(400).body("이미지를 삭제할 수 없습니다.");
        }


    }




}
