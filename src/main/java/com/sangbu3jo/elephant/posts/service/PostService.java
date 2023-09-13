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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final S3Service s3Service;


    //카테고리
    private final Category project = Category.COOPERATION_PROJECT;
    private final Category study = Category.DEVELOPMENT_STUDY;
    private final Category previousExam = Category.PREVIOUS_EXAM;

    private final Category forumBoard = Category.FORUM_BOARD;


    //게시글 생성
    public PostResponseDto createPost(User user, PostRequestDto postRequestDto) {

        //유저 확인
        User loginUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));


        //게시물 생성
        Post post = new Post(postRequestDto, user);

        //category 나누기
        if (postRequestDto.getCategory().equals(Category.COOPERATION_PROJECT)) {
            post.setCategory(project);
        } else if (postRequestDto.getCategory().equals(Category.DEVELOPMENT_STUDY)) {
            post.setCategory(study);
        } else if (postRequestDto.getCategory().equals(Category.PREVIOUS_EXAM)) {
            post.setCategory(previousExam);
        } else if (postRequestDto.getCategory().equals(Category.FORUM_BOARD)) {
            post.setCategory(forumBoard);

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
    public void modifiedPost(Post post, PostRequestDto postRequestDto, Long postId) {


        //category 나누기
        if (postRequestDto.getCategory().equals(Category.COOPERATION_PROJECT)) {
            post.setCategory(project);
        } else if (postRequestDto.getCategory().equals(Category.DEVELOPMENT_STUDY)) {
            post.setCategory(study);
        } else if (postRequestDto.getCategory().equals(Category.PREVIOUS_EXAM)) {
            post.setCategory(previousExam);
        } else if (postRequestDto.getCategory().equals(Category.FORUM_BOARD)) {
            post.setCategory(forumBoard);

        } else {
            throw new NullPointerException("해당 카테고리를 존재하지 않습니다.");
        }

        if(postRequestDto.getNewImg() == false) {
            //새로 들어온 이미지가 없으니깐 이미지를 삭제 시켜야 함
            Post postImg = findById(postId);
            s3Service.deleteFile(postImg.getFiles(), postImg);
        }

        post.updatePost(postRequestDto);
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
    public Page<PostResponseDto> getCategoryPost(Integer category, Pageable pageable, Integer pageNo) {

        // 작성일자 순으로 내림차순 정렬 객체 생성
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        // 정렬 객체를 통해 pageNo와 한 페이지당 갯수 설정하여 pageable 객체에 입력. 
        pageable = PageRequest.of(pageNo, 5, sort);

        // 각 카테고리별 데이터 가져와서 객체에 입력.
        Page<Post> projectList;
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
            case 4:
                projectList = postRepository.findAllByCategoryOrderByCreatedAtDesc(Category.FORUM_BOARD, pageable);
                break;
            default:
                throw new IllegalArgumentException("해당 카테고리가 존재하지 않습니다.");
        }

        // dto 타입으로 반환
        return projectList.map(PostResponseDto::new);
    }

    //게시글 카테고리 검색 조회
    //슬라이스 구현
    public Page<PostResponseDto> getSearchTitle(Integer category, Pageable pageable, Integer pageNo,
                                                String title) {
        // 작성일자 순으로 내림차순 정렬 객체 생성
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        // 정렬 객체를 통해 pageNo와 한 페이지당 갯수 설정하여 pageable 객체에 입력.
        pageable = PageRequest.of(pageNo, 5, sort);

        // 각 카테고리별 데이터 가져와서 객체에 입력.
        Page<Post> searchList;

        switch (category) {
            case 1:
                searchList = postRepository.findAllByCategoryAndTitleContainingOrderByCreatedAtDesc(
                        Category.COOPERATION_PROJECT, title, pageable);
                break;
            case 2:
                searchList = postRepository.findAllByCategoryAndTitleContainingOrderByCreatedAtDesc(
                        Category.DEVELOPMENT_STUDY, title, pageable);
                break;
            case 3:
                searchList = postRepository.findAllByCategoryAndTitleContainingOrderByCreatedAtDesc(
                        Category.PREVIOUS_EXAM, title, pageable);
                break;
            case 4:
                searchList = postRepository.findAllByCategoryAndTitleContainingOrderByCreatedAtDesc(
                        Category.FORUM_BOARD, title, pageable);
                break;
            default:
                throw new IllegalArgumentException("해당 카테고리가 존재하지 않습니다.");
        }

        return searchList.map(PostResponseDto::new);
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


        List<Post> projectList;

        projectList = postRepository.findTop5ByCategoryOrderByCreatedAtDesc(project);

        List<PostResponseDto> postResponseDtoList = new ArrayList<>();

        for (Post post : projectList) {
            postResponseDtoList.add(new PostResponseDto(post));
        }


        return postResponseDtoList;


    }

    //스터디 카테고리 조회
    public List<PostResponseDto> getStudy() {

        List<Post> projectList;

        projectList = postRepository.findTop5ByCategoryOrderByCreatedAtDesc(study);

        List<PostResponseDto> postResponseDtoList = new ArrayList<>();

        for (Post post : projectList) {
            postResponseDtoList.add(new PostResponseDto(post));
        }


        return postResponseDtoList;

    }

    //문제은행 카테고리 조회
    public List<PostResponseDto> getExam() {

        List<Post> projectList;

        projectList = postRepository.findTop5ByCategoryOrderByCreatedAtDesc(previousExam);

        List<PostResponseDto> postResponseDtoList = new ArrayList<>();

        for (Post post : projectList) {
            postResponseDtoList.add(new PostResponseDto(post));
        }

        return postResponseDtoList;

    }

    //자유게시판
    public List<PostResponseDto> getForum() {

        List<Post> projectList;

        projectList = postRepository.findTop5ByCategoryOrderByCreatedAtDesc(forumBoard);

        List<PostResponseDto> postResponseDtoList = new ArrayList<>();

        for (Post post : projectList) {
            postResponseDtoList.add(new PostResponseDto(post));
        }

        return postResponseDtoList;
    }

    public Post findById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시물은 존재하지 않습니다."));

        return post;
    }

}















