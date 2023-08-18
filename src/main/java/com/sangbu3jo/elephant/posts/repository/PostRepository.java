package com.sangbu3jo.elephant.posts.repository;

import com.sangbu3jo.elephant.posts.entity.Category;
import com.sangbu3jo.elephant.posts.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findAllByCategoryOrderByCreatedAtDesc(Category category, Pageable pageable);

    Page<Post> findAllByTitleContainingOrderByCreatedAtDesc(String title, Pageable pageable);


}
