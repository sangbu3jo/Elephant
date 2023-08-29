package com.sangbu3jo.elephant.posts.service;

import com.sangbu3jo.elephant.posts.dto.PostRequestDto;
import com.sangbu3jo.elephant.posts.entity.Category;
import com.sangbu3jo.elephant.posts.entity.Post;
import com.sangbu3jo.elephant.posts.repository.PostRepository;
import com.sangbu3jo.elephant.users.entity.User;
import com.sangbu3jo.elephant.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class S3Service {

    // 의존성 주입
    private final PostRepository postRepository;

    private final S3UploaderService s3UploaderService;

    private final UserRepository userRepository;

    //게시물 작성
    @Transactional
    public Long keepPost(MultipartFile image, PostRequestDto postRequestDto, Category category, User user) throws IOException {


        Post post = new Post(postRequestDto, user);

        if (!image.isEmpty()) {
            String storedFileName = s3UploaderService.upload(image, "image");
            post.setFiles(storedFileName);
        }

        post.setCategory(category);

        post.setCompleted(false);

        //저장
        Post savePost = postRepository.save(post);

        return savePost.getId();
    }


    @Transactional
    public void modifiedPost(MultipartFile image,
                             Category category,
                             PostRequestDto postRequestDto,
                             Post post) throws IOException {


        if (category.equals(Category.COOPERATION_PROJECT)) {
            post.setCategory(Category.COOPERATION_PROJECT);
        } else if (category.equals(Category.DEVELOPMENT_STUDY)) {
            post.setCategory(Category.DEVELOPMENT_STUDY);
        } else if (category.equals(Category.PREVIOUS_EXAM)) {
            post.setCategory(Category.PREVIOUS_EXAM);

        } else if (category.equals(Category.FORUM_BOARD)) {
            post.setCategory(Category.PREVIOUS_EXAM);
        } else {
            throw new NullPointerException("해당 카테고리를 존재하지 않습니다.");
        }


        if (!image.isEmpty()) {
            String storedFileName = s3UploaderService.upload(image, "image");
            post.setFiles(storedFileName);
        }

        post.updatePost(postRequestDto);


    }


}

