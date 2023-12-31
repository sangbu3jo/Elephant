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
                             Post post
                             ) throws IOException {


        switch (category) {
            case COOPERATION_PROJECT:
                post.setCategory(Category.COOPERATION_PROJECT);
                break;
            case DEVELOPMENT_STUDY:
                post.setCategory(Category.DEVELOPMENT_STUDY);
                break;
            case PREVIOUS_EXAM:
                post.setCategory(Category.PREVIOUS_EXAM);
                break;
            case FORUM_BOARD:
                post.setCategory(Category.FORUM_BOARD);
                break;
            default:
                throw new IllegalArgumentException("해당 카테고리를 존재하지 않습니다.");
        }




        if (!image.isEmpty()) {
            String storedFileName = s3UploaderService.upload(image, "image");
            post.setFiles(storedFileName);

        }



        //post 값 업데이트
        post.updatePost(postRequestDto);






    }


    //이미지 삭제
    @Transactional
    public void deleteFile(String fileUrl, Post post) {



        String s3Key = fileUrl.substring("https://sangbusamjoelephant.s3.ap-northeast-2.amazonaws.com/".length());

        s3UploaderService.fileDelete(s3Key);


        post.setFiles(null);
    }


    public Post findById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시물은 존재하지 않습니다."));

        return post;
    }
}

