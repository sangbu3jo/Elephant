package com.sangbu3jo.elephant.posts.service;

import com.sangbu3jo.elephant.posts.dto.PostRequestDto;
import com.sangbu3jo.elephant.posts.dto.PostResponseDto;
import com.sangbu3jo.elephant.posts.entity.Category;
import com.sangbu3jo.elephant.posts.entity.Post;
import com.sangbu3jo.elephant.posts.repository.PostRepository;
import com.sangbu3jo.elephant.users.entity.User;
import com.sangbu3jo.elephant.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    //카테고리
    private final Category project = Category.COOPERATION_PROJECT;
    private final Category study = Category.DEVELOPMENT_STUDY;
    private final Category previousExam = Category.PREVIOUS_EXAM;


    public PostResponseDto createPost(User user, PostRequestDto postRequestDto) {

        //유저 확인
        User loginUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));


        //게시물 생성

        Post post = new Post(postRequestDto, user);

        //category 나누기
        if (postRequestDto.getCategory() == 1) {
            post.setCategory(project);
        } else if (postRequestDto.getCategory() == 2) {
            post.setCategory(study);

        } else if (postRequestDto.getCategory() == 3) {
            post.setCategory(previousExam);
        } else {
            throw new NullPointerException("해당 카테고리를 존재하지 않습니다.");
        }

        //저장
        postRepository.save(post);

        return new PostResponseDto(post);


    }


    //게시물 수정
    @Transactional
    public void modifiedPost(PostRequestDto postRequestDto, Long postID) {


        //게시물
        Post post = postRepository.findById(postID)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));


        post.updatePost(postRequestDto);

        //category 나누기
        if (postRequestDto.getCategory() == 1) {
            post.setCategory(project);
        } else if (postRequestDto.getCategory() == 2) {
            post.setCategory(study);

        } else if (postRequestDto.getCategory() == 3) {
            post.setCategory(previousExam);
        } else {
            throw new NullPointerException("해당 카테고리가 존재하지 않습니다.");
        }


    }

    //게시글 삭제
    @Transactional
    public void deletePost(Post post) {


        //삭제
        postRepository.delete(post);


    }


    //게시글 카테고리 별 전체 조회
    public List<PostResponseDto> getCategoryPost(Integer category, Integer pageNum) {


        List<PostResponseDto> postResponseDtoList = new ArrayList<>();
        Page<Post> projectList;

        // 페이징 구현
        Pageable pageable = PageRequest.of(pageNum - 1,5); //페이지 번호는 0부터 시작함, 한 페이지에 게시물 갯수


        if (category == 1) {
            projectList = postRepository.findAllByCategoryOrderByCreatedAtDesc(project, pageable);

        } else if (category == 2) {
            projectList = postRepository.findAllByCategoryOrderByCreatedAtDesc(study, pageable);

        } else if (category == 3) {
            projectList = postRepository.findAllByCategoryOrderByCreatedAtDesc(previousExam, pageable);

        } else throw new NullPointerException("해당 카테고리가 존재하지 않습니다.");

        for (Post post : projectList)
            postResponseDtoList.add(new PostResponseDto(post));


        return postResponseDtoList;
    }

    //게시물 단건 조회(댓글 포함)
    @Transactional
    public PostResponseDto getPost(Long postId, User user) {

//        로그인한 회원  확인
        User loginUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원은 존재하지 않습니다."));

        //게시물 확인
        Post findedPost = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));

        //조회할 때마다 조회수 올라감
        int cnt = findedPost.getView_cnt() == null ? 1 : findedPost.getView_cnt() + 1;
        findedPost.setView_cnt(cnt);

        PostResponseDto postResponseDto = new PostResponseDto(findedPost);


        return postResponseDto;
    }


//    public List<PostResponseDto> getAllPosts() {
//
//        List<Post> postList = new ArrayList<>();
//        List<Post> projectList = postRepository.findAllByOrderByCategoryAsc();
//        List<PostResponseDto> postResponseDtoList = new ArrayList<>();
//
//
//            for (int i = 0; i <= projectList.size() + 1; i++) {
//                if (postList.size() % 5 == 0) {
//                    postList.get(i).equals(postList.get(i + 1));
//                    continue;
//                } else postList.add(projectList.get(i));
//
//            }
//
//
//
//
//
//
//        return postResponseDtoList;
//    }
}








