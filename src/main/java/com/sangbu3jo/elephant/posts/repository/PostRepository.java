package com.sangbu3jo.elephant.posts.repository;

import com.sangbu3jo.elephant.posts.entity.Category;
import com.sangbu3jo.elephant.posts.entity.Post;
import com.sangbu3jo.elephant.users.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {




    Page<Post> findAllByCategoryOrderByCreatedAtAsc(Category category, Pageable pageable);


    Page<Post> findAllByCategoryOrderByViewCntDesc(Category category, Pageable pageable);


    Page<Post> findAllByCategoryOrderByCommentListDesc(Category category, Pageable pageable);

    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Post> findAllByCategoryOrderByCreatedAtDesc(Category category, Pageable pageable);

    List<Post> findTop5ByCategoryOrderByCreatedAtDesc(Category category);

//    Slice<Post> findAllByCategoryOrderByCreatedAtDesc(Category category, Pageable pageable);


    Slice<Post> findAllByCategoryAndTitleContainingOrderByCreatedAtDesc(Category category, String title, Pageable pageable);

    List<Post> findByUser(User user);


    void deleteAllByUser(User user);

    Optional<Post> findByFiles(String fileName);

}
