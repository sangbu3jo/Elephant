package com.sangbu3jo.elephant.posts.repository;

import com.sangbu3jo.elephant.posts.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

}
