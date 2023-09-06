package com.sangbu3jo.elephant.users.service;

import com.sangbu3jo.elephant.posts.dto.PostCommentResponseDto;
import com.sangbu3jo.elephant.posts.dto.PostResponseDto;
import com.sangbu3jo.elephant.posts.entity.Category;
import com.sangbu3jo.elephant.posts.entity.Post;
import com.sangbu3jo.elephant.posts.entity.PostComment;
import com.sangbu3jo.elephant.posts.repository.PostCommentRepository;
import com.sangbu3jo.elephant.posts.repository.PostRepository;
import com.sangbu3jo.elephant.posts.service.S3Service;
import com.sangbu3jo.elephant.posts.service.S3UploaderService;
import com.sangbu3jo.elephant.users.dto.ProfileRequestDto;
import com.sangbu3jo.elephant.users.dto.UpdateUserCommentsDto;
import com.sangbu3jo.elephant.users.dto.UpdateUserPostsDto;
import com.sangbu3jo.elephant.users.dto.UserResponseDto;
import com.sangbu3jo.elephant.users.entity.User;
import com.sangbu3jo.elephant.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private PostCommentRepository postCommentRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private S3UploaderService s3UploaderService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        // Mockito 초기화
        MockitoAnnotations.openMocks(this);

        // UserService 초기화
        userService = new UserService(userRepository,postRepository,postCommentRepository,passwordEncoder,s3UploaderService);
    }

    @Test
    @DisplayName("유저 정보 조회")
    void getUserInfo() {
        // given
        User user = new User();
        user.setId(1L);
        user.setNickname("user");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserService userService = new UserService(userRepository,postRepository,postCommentRepository,passwordEncoder,s3UploaderService);

        // when
        UserResponseDto userResponseDto = userService.getUserInfo(user);

        // then
        assertEquals(user.getNickname(), userResponseDto.getNickname());
    }

    @Test
    @DisplayName("프로필 수정")
    void updateProfile() {
        // given
        User user = new User();
        user.setId(1L);
        user.setNickname("user");
        user.setIntroduction("hello");

        ProfileRequestDto profileRequestDto = new ProfileRequestDto();
        profileRequestDto.setNickname("updateUser");
        profileRequestDto.setIntroduction("updateHello");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserService userService = new UserService(userRepository,postRepository,postCommentRepository,passwordEncoder,s3UploaderService);

        // when
        String result = userService.updateProfile(user, profileRequestDto);

        // then
        assertEquals("프로필 수정이 완료되었습니다.", result);
        assertEquals("updateUser", user.getNickname());
        assertEquals("updateHello", user.getIntroduction());
    }

    @Test
    @DisplayName("회원 탈퇴")
    void signOut() {
        // given
        User user = new User();
        user.setId(1L);

        // Mock 설정
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserService userService = new UserService(userRepository,postRepository,postCommentRepository,passwordEncoder,s3UploaderService);

        // when
        String result = userService.signOut(user);

        // then
        assertEquals("회원탈퇴가 완료되었습니다.", result);
    }

    @Test
    @DisplayName("작성한 게시물 조회")
    void getUserPosts() {
        // given
        User user = new User();
        user.setId(1L);

        Post post1 = new Post();
        post1.setTitle("post1");
        post1.setContent("post1");
        post1.setCategory(Category.COOPERATION_PROJECT);
        post1.setCompleted(false);
        post1.setUser(user);

        Post post2 = new Post();
        post2.setTitle("post2");
        post2.setContent("post2");
        post2.setCategory(Category.COOPERATION_PROJECT);
        post2.setCompleted(false);
        post2.setUser(user);

        List<Post> postList = new ArrayList<>();
        postList.add(post1);
        postList.add(post2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findByUser(user)).thenReturn(postList);

        // when
        List<PostResponseDto> userPosts = userService.getUserPosts(user);

        // then
        assertEquals(postList.size(), userPosts.size());
        assertEquals(post1.getTitle(), userPosts.get(0).getTitle());
        assertEquals(post2.getTitle(), userPosts.get(1).getTitle());
    }

    @Test
    @DisplayName("작성한 게시글 수정")
    void updateUserPosts() {
        // given
        User user = new User();
        user.setId(1L);

        Post post1 = new Post();
        post1.setTitle("post1");
        post1.setContent("post1");
        post1.setCategory(Category.COOPERATION_PROJECT);
        post1.setCompleted(false);
        post1.setUser(user);

        UpdateUserPostsDto updateRequest = new UpdateUserPostsDto();
        updateRequest.setTitle("updatedTitle");
        updateRequest.setContent("updatedContent");

        when(postRepository.findById(1L)).thenReturn(Optional.of(post1));

        // when
        String result = userService.updateUserPosts(user, updateRequest, 1L);

        // then
        assertEquals("게시글 수정되었습니다", result);
        assertEquals("updatedTitle", post1.getTitle());
        assertEquals("updatedContent", post1.getContent());
    }

    @Test
    @DisplayName("작성한 게시글 삭제")
    void deleteUserPosts() {
        // given
        User user = new User();
        user.setId(1L);

        Post post1 = new Post();
        post1.setTitle("post1");
        post1.setContent("post1");
        post1.setCategory(Category.COOPERATION_PROJECT);
        post1.setCompleted(false);
        post1.setUser(user);

//        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.findById(1L)).thenReturn(Optional.of(post1));

        // when
        String result = userService.deleteUserPosts(user, 1L);

        // then
//        assertEquals("게시글이 삭제되었습니다.", result);
    }

    @Test
    @DisplayName("작성한 댓글 조회")
    void getUserComments() {
        // given
        User user = new User();
        user.setId(1L);

        Post post = new Post();
        post.setId(1L);

        PostComment postComment1 = new PostComment();
        postComment1.setContent("content1 hello");
        postComment1.setUser(user);
        postComment1.setPost(post);

        PostComment postComment2 = new PostComment();
        postComment2.setContent("content2 hello");
        postComment2.setUser(user);
        postComment2.setPost(post);

        List<PostComment> postCommentList = new ArrayList<>();
        postCommentList.add(postComment1);
        postCommentList.add(postComment2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postCommentRepository.findByUser(user)).thenReturn(postCommentList);

        // when
        List<PostCommentResponseDto> postComment = userService.getUserComments(user);

        // then
        assertEquals(postCommentList.size(), postComment.size());
        assertEquals(postComment1.getContent(), postComment.get(0).getContent());
        assertEquals(postComment2.getContent(), postComment.get(1).getContent());
    }

    @Test
    @DisplayName("작성한 댓글 수정")
    void updateUserComments() {
        // given
        User user = new User();
        user.setId(1L);

        Post post = new Post();
        post.setId(1L);

        PostComment postComment1 = new PostComment();
        postComment1.setId(1L);
        postComment1.setContent("content1 hello");
        postComment1.setUser(user);
        postComment1.setPost(post);

        UpdateUserCommentsDto updateRequest = new UpdateUserCommentsDto();
        updateRequest.setContent("updatedContent");

        when(postCommentRepository.findById(1L)).thenReturn(Optional.of(postComment1));

        // when
        String result = userService.updateUserComments(user, updateRequest, 1L);

        // then
        assertEquals("댓글이 수정되었습니다.", result);
        assertEquals("updatedContent", postComment1.getContent());
    }

    @Test
    @DisplayName("작성한 댓글 삭제")
    void deleteUserComments() {
        // given
        User user = new User();
        user.setId(1L);

        PostComment postComment1 = new PostComment();
        postComment1.setId(1L);
        postComment1.setContent("content1 hello");
        postComment1.setUser(user);

        when(postCommentRepository.findById(1L)).thenReturn(Optional.of(postComment1));

        // when
        String result = userService.deleteUserComments(user, 1L);

        // then
        assertEquals("댓글이 삭제되었습니다.", result);
    }
}