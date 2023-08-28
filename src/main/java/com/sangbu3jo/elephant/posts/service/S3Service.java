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

    @Transactional
    public Long keepPost(MultipartFile image, PostRequestDto postRequestDto, Category category, User user) throws IOException {


        Post post = new Post(postRequestDto,user);

        if (!image.isEmpty()) {
            String storedFileName = s3UploaderService.upload(image, "image");
            post.setFiles(storedFileName);
        }

        post.setCategory(category);

        post.setCompleted(false);

//       Post newPost = new Post(post,user);





        //저장
        Post savedFile = postRepository.save(post);

        return savedFile.getId();
    }
}