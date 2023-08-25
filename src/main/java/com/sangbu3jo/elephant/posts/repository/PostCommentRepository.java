package com.sangbu3jo.elephant.posts.repository;

import com.sangbu3jo.elephant.posts.entity.PostComment;
import com.sangbu3jo.elephant.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    List<PostComment> findByUser(User user);
    void deleteAllByUser(User user);
}
