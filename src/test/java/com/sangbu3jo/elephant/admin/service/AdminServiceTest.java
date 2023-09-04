package com.sangbu3jo.elephant.admin.service;

import com.sangbu3jo.elephant.posts.dto.PostCommentRequestDto;
import com.sangbu3jo.elephant.posts.dto.PostRequestDto;
import com.sangbu3jo.elephant.posts.entity.Post;
import com.sangbu3jo.elephant.posts.entity.PostComment;
import com.sangbu3jo.elephant.posts.repository.PostCommentRepository;
import com.sangbu3jo.elephant.posts.repository.PostRepository;
import com.sangbu3jo.elephant.users.entity.User;
import com.sangbu3jo.elephant.users.entity.UserRoleEnum;
import com.sangbu3jo.elephant.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private PostCommentRepository postCommentRepository;

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        // Mockito 초기화
        MockitoAnnotations.openMocks(this);

        // AdminService 초기화
        adminService = new AdminService(userRepository, postRepository, postCommentRepository);
    }

    @Test
    @DisplayName("회원 전체 조회")
    void getAllUsers() {
        // given
        User user1 = new User();
        user1.setNickname("user1");
        user1.setIntroduction("user1 gdgd");
        user1.setRole(UserRoleEnum.ADMIN);

        User user2 = new User();
        user2.setNickname("user2");
        user2.setIntroduction("user2 gdgd");
        user2.setRole(UserRoleEnum.USER);

        User user3 = new User();
        user3.setNickname("user3");
        user3.setIntroduction("user3 gdgd");
        user3.setRole(UserRoleEnum.USER);

        List<User> userList = new ArrayList<>();
        userList.add(user1);
        userList.add(user2);
        userList.add(user3);

        UserRepository userRepository = Mockito.mock(UserRepository.class);
        Mockito.when(userRepository.findAll()).thenReturn(userList);

        // when
        List<User> resultList = userRepository.findAll();

        // then
        assertEquals(userList.size(), resultList.size());
        assertEquals(user1.getNickname(), resultList.get(0).getNickname());
        assertEquals(user2.getNickname(), resultList.get(1).getNickname());
        assertEquals(user3.getNickname(), resultList.get(2).getNickname());
    }

    @Test
    @DisplayName("게시물 전체 조회")
    void getAllUserPosts() {
        // given
        Post post1 = new Post();
        post1.setTitle("post1");
        post1.setContent("post1c");

        Post post2 = new Post();
        post2.setTitle("post2");
        post2.setContent("post2c");

        Post post3 = new Post();
        post3.setTitle("post3");
        post3.setContent("post3c");

        List<Post> postList = new ArrayList<>();
        postList.add(post1);
        postList.add(post2);
        postList.add(post3);

        PostRepository postRepository = Mockito.mock(PostRepository.class);
        Mockito.when(postRepository.findAll()).thenReturn(postList);

        // when
        List<Post> resultList = postRepository.findAll();

        // then
        assertEquals(postList.size(), resultList.size());
        assertEquals(post1.getTitle(), resultList.get(0).getTitle());
        assertEquals(post2.getTitle(), resultList.get(1).getTitle());
        assertEquals(post3.getTitle(), resultList.get(2).getTitle());
    }

    @Test
    @DisplayName("댓글 전체 조회")
    void getALlUserComments() {
        // given
        PostComment postComment1 = new PostComment();
        postComment1.setContent("content1 hello");

        PostComment postComment2 = new PostComment();
        postComment2.setContent("content1 hello");

        PostComment postComment3 = new PostComment();
        postComment3.setContent("content1 hello");

        List<PostComment> postCommentList = new ArrayList<>();
        postCommentList.add(postComment1);
        postCommentList.add(postComment2);
        postCommentList.add(postComment3);

        PostCommentRepository postCommentRepository = Mockito.mock(PostCommentRepository.class);
        Mockito.when(postCommentRepository.findAll()).thenReturn(postCommentList);

        // when
        List<PostComment> resultList = postCommentRepository.findAll();

        // then
        assertEquals(postCommentList.size(), resultList.size());
        assertEquals(postComment1.getContent(), resultList.get(0).getContent());
        assertEquals(postComment2.getContent(), resultList.get(1).getContent());
        assertEquals(postComment3.getContent(), resultList.get(2).getContent());
    }

    @Test
    @DisplayName("회원 권한 수정")
    void updateAuth() {
        // given
        User adminUser = new User();
        adminUser.setRole(UserRoleEnum.ADMIN);

        User userToUpdate = new User();
        userToUpdate.setId(1L);
        userToUpdate.setRole(UserRoleEnum.USER);

        UserRepository userRepository = Mockito.mock(UserRepository.class);
        Mockito.when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(userToUpdate));

        AdminService adminService = new AdminService(userRepository, postRepository, postCommentRepository);

        // when
        String result = adminService.updateAuth(adminUser, 1L);

        // then
        assertEquals("권한이 수정되었습니다.", result);
    }

    @Test
    @DisplayName("회원 탈퇴")
    void deleteAuth() {
        // given
        User adminUser = new User();
        adminUser.setRole(UserRoleEnum.ADMIN);

        User userToDelete = new User();
        userToDelete.setId(1L);

        UserRepository userRepository = Mockito.mock(UserRepository.class);
        Mockito.when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(userToDelete));

        AdminService adminService = new AdminService(userRepository, postRepository, postCommentRepository);

        // when
        String result = adminService.deleteAuth(adminUser, 1L);

        // then
        assertEquals("회원이 탈퇴처리 되었습니다.", result);
    }

    @Test
    @DisplayName("게시글 숨김처리")
    void adminUpdatePost() {
        // given
        User adminUser = new User();
        adminUser.setRole(UserRoleEnum.ADMIN);

        PostRepository postRepository = Mockito.mock(PostRepository.class);
        Mockito.when(postRepository.findById(1L)).thenReturn(java.util.Optional.of(new Post()));

        AdminService adminService = new AdminService(userRepository, postRepository, postCommentRepository);

        // when
        String result = adminService.adminUpdatePost(adminUser, new PostRequestDto(), 1L);

        // then
        assertEquals("게시글 숨김처리가 완료되었습니다", result);
    }

    @Test
    @DisplayName("게시글 삭제")
    void adminDeletePost() {
        // given
        User adminUser = new User();
        adminUser.setRole(UserRoleEnum.ADMIN);

        PostRepository postRepository = Mockito.mock(PostRepository.class);
        Mockito.when(postRepository.findById(1L)).thenReturn(java.util.Optional.of(new Post()));

        AdminService adminService = new AdminService(userRepository, postRepository, postCommentRepository);

        // when
        String result = adminService.adminDeletePost(adminUser, 1L);

        // then
        assertEquals("게시글 삭제가 완료되었습니다.", result);
    }

    @Test
    @DisplayName("댓글 수정")
    void adminUpdateComment() {
        // given
        User adminUser = new User();
        adminUser.setRole(UserRoleEnum.ADMIN);

        PostCommentRepository postCommentRepository = Mockito.mock(PostCommentRepository.class);
        Mockito.when(postCommentRepository.findById(1L)).thenReturn(java.util.Optional.of(new PostComment()));

        AdminService adminService = new AdminService(userRepository, postRepository, postCommentRepository);

        // when
        String result = adminService.adminUpdateComment(1L, adminUser, new PostCommentRequestDto());

        // then
        assertEquals("댓글 숨김처리가 완료되었습니다.", result);
    }

    @Test
    @DisplayName("댓글 삭제")
    void adminDeleteComment() {
        // given
        User adminUser = new User();
        adminUser.setRole(UserRoleEnum.ADMIN);

        PostCommentRepository postCommentRepository = Mockito.mock(PostCommentRepository.class);
        Mockito.when(postCommentRepository.findById(1L)).thenReturn(java.util.Optional.of(new PostComment()));

        AdminService adminService = new AdminService(userRepository, postRepository, postCommentRepository);

        // when
        String result = adminService.adminDeleteComment(1L, adminUser);

        // then
        assertEquals("댓글 삭제가 완료되었습니다.", result);
    }

}