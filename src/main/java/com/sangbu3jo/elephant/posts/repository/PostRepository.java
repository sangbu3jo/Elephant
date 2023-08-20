package com.sangbu3jo.elephant.posts.repository;

import com.sangbu3jo.elephant.posts.entity.Category;
import com.sangbu3jo.elephant.posts.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findAllByCategoryOrderByCreatedAtDesc(Category category, Pageable pageable);


    Page<Post> findAllByCategoryOrderByCreatedAtAsc(Category category, Pageable pageable);

    Page<Post> findAllByCategoryOrderByViewCntDesc(Category category, Pageable pageable);

    Page<Post> findAllByCategoryOrderByCommentListDesc(Category category, Pageable pageable);

//    Page<Post> findAllByTitleContainingOrderByCreatedAtDesc(String title, Pageable pageable);

    Slice<Post> findAllByCategoryAndTitleContainingOrderByCreatedAtDesc(Category category, String title, Pageable pageable);


}
