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


    /**
     * 게시글 이미지 추가 생성
     * @param image 이미지 url
     * @param category 카테고리 선택
     * @param postRequestDto 클라이언트에서 보낸 게시글 정보
     * @param userDetails 로그인한 유저
     * @return id 값으로 게시글 확인
     * @throws IOException 예외처리
     */

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

    /**
     * 게시글 수정
     * @param image 새 이미지
     * @param category 새 카테고리
     * @param postRequestDto 새 게시글 정보 값
     * @param userDetails 로그인한 회원 정보
     * @param post_id 수정 할 게시글 id 값
     * @return 성공 실패 ResponseEntity 반환
     * @throws IOException 예외처리
     */
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

    /**
     * 이미지 삭제
     * @param fileUrl 삭제할 이미지 url
     * @param post_id 게시글 id
     * @param userDetails 로그인한 회원 정보
     * @return 성공 실패 ResponseEntity 반환
     * @throws IOException 예외처리
     */
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
