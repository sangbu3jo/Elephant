package com.sangbu3jo.elephant.posts.service;

import com.sangbu3jo.elephant.posts.dto.PostRequestDto;
import com.sangbu3jo.elephant.posts.entity.Category;
import com.sangbu3jo.elephant.posts.entity.Post;
import com.sangbu3jo.elephant.posts.repository.PostRepository;
import com.sangbu3jo.elephant.users.entity.User;
import com.sangbu3jo.elephant.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class S3ServiceTest {

    private S3Service s3Service;

    @Mock
    private PostRepository postRepository;

    @Mock
    private S3UploaderService s3UploaderService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        s3Service = new S3Service(postRepository, s3UploaderService, userRepository);
    }

    @Test
    @DisplayName("이미지 포함 게시글 생성")
    public void testKeepPost() throws IOException {
        // 가짜 데이터 및 파일 생성
        MultipartFile fakeImage = mock(MultipartFile.class);
        when(fakeImage.isEmpty()).thenReturn(false);

        PostRequestDto fakePostRequestDto = new PostRequestDto();
        Category fakeCategory = Category.COOPERATION_PROJECT;
        User fakeUser = new User();
        fakeUser.setId(1L);

        // userRepository.findById 메서드에 대한 Mock 설정
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(fakeUser));

        // s3UploaderService.upload 메서드에 대한 Mock 설정
        when(s3UploaderService.upload(fakeImage, "image")).thenReturn("fake-stored-filename");

        // postRepository.save 메서드에 대한 Mock 설정
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post savedPost = invocation.getArgument(0);
            savedPost.setId(123L); // 가짜 ID 설정
            return savedPost;
        });

        // 테스트 실행
        Long postId = s3Service.keepPost(fakeImage, fakePostRequestDto, fakeCategory, fakeUser);

        // 결과 확인
        assertNotNull(postId);
        assertEquals(123L, postId); // 위에서 가짜 ID 설정한 값과 일치하는지 확인
    }

    @Test
    @DisplayName("이미지 포함 게시글 수정")
    public void testModifiedPost() throws IOException {
        // 가짜 데이터 및 파일 생성
        MultipartFile fakeImage = mock(MultipartFile.class);
        when(fakeImage.isEmpty()).thenReturn(false);

        Category fakeCategory = Category.COOPERATION_PROJECT;

        PostRequestDto fakePostRequestDto = new PostRequestDto();
        fakePostRequestDto.setTitle("수정된 제목");
        fakePostRequestDto.setContent("수정된 내용");

        Post fakePost = new Post();
        fakePost.setCategory(Category.DEVELOPMENT_STUDY);
        fakePost.setTitle("기존 제목");
        fakePost.setContent("기존 내용");

        // 테스트 실행
        s3Service.modifiedPost(fakeImage, fakeCategory, fakePostRequestDto, fakePost);

        // 결과 확인
        assertEquals(Category.COOPERATION_PROJECT, fakePost.getCategory()); // 카테고리 업데이트 확인
        assertEquals("수정된 제목", fakePost.getTitle()); // 제목 업데이트 확인
        assertEquals("수정된 내용", fakePost.getContent()); // 내용 업데이트 확인
    }


}
