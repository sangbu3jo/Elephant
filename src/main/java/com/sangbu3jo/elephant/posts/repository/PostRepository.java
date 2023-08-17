package com.sangbu3jo.elephant.posts.repository;

import com.sangbu3jo.elephant.posts.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

}
