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
import org.springframework.stereotype.Service;

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

    @Transactional
    public void deletePost(Post post) {


        //삭제
        postRepository.delete(post);


    }
}

