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
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    //카테고리
    private final Category project = Category.COOPERATION_PROJECT;
    private final Category study = Category.DEVELOPMENT_STUDY;
    private final Category previousExam = Category.PREVIOUS_EXAM;


    //게시글 생성
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

        //게시글을 생성하면 모집 여부 모집 중
        postRequestDto.setCompleted(false);

        //저장
        postRepository.save(post);

        return new PostResponseDto(post);


    }


    //게시물 수정
    @Transactional
    public void modifiedPost(PostRequestDto postRequestDto, Long postID, User user) {

        //게시물
        Post post = postRepository.findById(postID)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시물이 존재하지 않습니다."));

        if (user.getId().equals(post.getUser().getId())) {
            switch (postRequestDto.getCategory()) {
                case 1:
                    post.setCategory(project);
                    break;
                case 2:
                    post.setCategory(study);
                    break;
                case 3:
                    post.setCategory(previousExam);
                    break;
                default:
                    throw new NullPointerException("해당 카테고리가 존재하지 않습니다.");
            }
            post.updatePost(postRequestDto);
        }
    }






    //게시글 삭제
    @Transactional
    public void deletePost(Post post) {




        //삭제
        postRepository.delete(post);


    }


    //메인페이지 게시글 전체 조회
    public List<PostResponseDto> getAllPosts() {

        Page<Post> allPost;
        List<PostResponseDto> postResponseDtoList = new ArrayList<>();

        // 페이징 구현
        Pageable pageable = PageRequest.of(0, 15); //페이지 번호는 0부터 시작함, 한 페이지에 게시물 갯수


        //생성날짜 내림차순
        allPost = postRepository.findAllByOrderByCreatedAtDesc(pageable);



        for (Post post : allPost) {
            postResponseDtoList.add(new PostResponseDto(post));
        }


        return postResponseDtoList;


    }


    //게시글 카테고리 별 전체 조회
    public Page<PostResponseDto> getCategoryPost(Integer category, int page, int size, String sortBy, boolean isAsc) {

        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);

        List<PostResponseDto> postResponseDtoList = new ArrayList<>();
        Page<Post> projectList;

        // 페이징 구현
        Pageable pageable = PageRequest.of(page - 1, size, sort); //페이지 번호는 0부터 시작함, 한 페이지에 게시물 갯수


        switch (category) {
            case 1:
                projectList = postRepository.findAllByCategoryOrderByCreatedAtDesc(Category.COOPERATION_PROJECT, pageable);
                break;
            case 2:
                projectList = postRepository.findAllByCategoryOrderByCreatedAtDesc(Category.DEVELOPMENT_STUDY, pageable);
                break;
            case 3:
                projectList = postRepository.findAllByCategoryOrderByCreatedAtDesc(Category.PREVIOUS_EXAM, pageable);
                break;
            default:
                throw new IllegalArgumentException("해당 카테고리가 존재하지 않습니다.");
        }



        for (Post post : projectList) {
            postResponseDtoList.add(new PostResponseDto(post));
        }

        return new PageImpl<>(postResponseDtoList, pageable, projectList.getTotalElements());
    }

    //게시글 카테고리 검색 조회
    //슬라이스 구현
    public List<PostResponseDto> getSearchTitle(Integer category, String title) {
        List<PostResponseDto> postResponseDtoList = new ArrayList<>();


        //슬라이스 구현
        Pageable pageable = PageRequest.of(0, 10); //페이지 번호는 0부터 시작함, 한 페이지에 게시물 갯수

        Slice<Post> searchList;

        switch (category) {
            case 1:
                searchList = postRepository.findAllByCategoryAndTitleContainingOrderByCreatedAtDesc(project, title, pageable);
                break;
            case 2:
                searchList = postRepository.findAllByCategoryAndTitleContainingOrderByCreatedAtDesc(study, title, pageable);
                break;
            case 3:
                searchList = postRepository.findAllByCategoryAndTitleContainingOrderByCreatedAtDesc(previousExam, title, pageable);
                break;
            default:
                throw new IllegalArgumentException("해당 카테고리가 존재하지 않습니다.");
        }

        return searchList.stream()
                .map(PostResponseDto::new)
                .collect(Collectors.toList());
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
        int cnt = findedPost.getViewCnt() == null ? 1 : findedPost.getViewCnt() + 1;
        findedPost.setView_cnt(cnt);

        PostResponseDto postResponseDto = new PostResponseDto(findedPost);


        return postResponseDto;
    }



    //프로젝트 카테고리 조회
    public List<PostResponseDto> getProject() {

        Pageable pageable = PageRequest.of(0, 1);

        Slice<Post> projectList;

        projectList = postRepository.findAllByCategoryOrderByCreatedAtDesc(project,pageable);

        List<PostResponseDto> postResponseDtoList = new ArrayList<>();

        for (Post post : projectList) {
            postResponseDtoList.add(new PostResponseDto(post));
        }

        return postResponseDtoList;


    }
    //스터디 카테고리 조회
    public List<PostResponseDto> getStudy() {

        Pageable pageable = PageRequest.of(0, 3);

        Slice<Post> projectList;

        projectList = postRepository.findAllByCategoryOrderByCreatedAtDesc(study,pageable);

        List<PostResponseDto> postResponseDtoList = new ArrayList<>();

        for (Post post : projectList) {
            postResponseDtoList.add(new PostResponseDto(post));
        }

        return postResponseDtoList;


    }
    //문제은행 카테고리 조회
    public List<PostResponseDto> getExam() {

        Pageable pageable = PageRequest.of(0, 2);

        Slice<Post> projectList;

        projectList = postRepository.findAllByCategoryOrderByCreatedAtDesc(previousExam,pageable);

        List<PostResponseDto> postResponseDtoList = new ArrayList<>();

        for (Post post : projectList) {
            postResponseDtoList.add(new PostResponseDto(post));
        }

        return postResponseDtoList;


    }
}












