package com.sangbu3jo.elephant.posts.service;

import com.sangbu3jo.elephant.posts.dto.PostRequestDto;
import com.sangbu3jo.elephant.posts.dto.PostResponseDto;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

public class PostServiceTest {

    private PostService postService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        postService = new PostService(userRepository, postRepository);
    }

    @Test
    @DisplayName("게시글 생성")
    public void testCreatePost() {
        // 가짜 유저 및 게시물 데이터 생성
        User fakeUser = new User();
        fakeUser.setId(1L);

        PostRequestDto fakePostRequestDto = new PostRequestDto();
        fakePostRequestDto.setCategory(Category.COOPERATION_PROJECT);
        fakePostRequestDto.setTitle("테스트 게시물");
        fakePostRequestDto.setContent("테스트 내용");

        // userRepository.findById 메서드에 대한 Mock 설정
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(fakeUser));

        // postRepository.save 메서드에 대한 Mock 설정
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post savedPost = invocation.getArgument(0);
            savedPost.setId(123L); // 가짜 ID 설정
            return savedPost;
        });

        // 테스트 실행
        PostResponseDto responseDto = postService.createPost(fakeUser, fakePostRequestDto);

        // 결과 확인
        assertNotNull(responseDto);
        assertEquals(123L, responseDto.getId()); // 위에서 가짜 ID 설정한 값과 일치하는지 확인
        assertFalse(responseDto.getCompleted()); // 모집 여부 확인
        // 여기에 필요한 추가적인 테스트 검증을 수행할 수 있습니다.
    }



    @Test
    @DisplayName("게시글 수정")
    public void testModifiedPost() {
        // 가짜 게시물 데이터 생성
        Post fakePost = new Post();
        fakePost.setCategory(Category.COOPERATION_PROJECT);
        fakePost.setTitle("기존 제목");
        fakePost.setContent("기존 내용");

        PostRequestDto modifiedDto = new PostRequestDto();
        modifiedDto.setCategory(Category.DEVELOPMENT_STUDY);
        modifiedDto.setTitle("수정된 제목");
        modifiedDto.setContent("수정된 내용");

        // postService.modifiedPost 메서드 호출
        postService.modifiedPost(fakePost, modifiedDto);

        // 게시물의 카테고리 및 내용이 수정되었는지 확인
        assertEquals(Category.DEVELOPMENT_STUDY, fakePost.getCategory());
        assertEquals("수정된 제목", fakePost.getTitle());
        assertEquals("수정된 내용", fakePost.getContent());
    }


}
