package com.sangbu3jo.elephant.users.repository;

import com.sangbu3jo.elephant.users.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

public interface UserRepository extends JpaRepository<User,Long>, QuerydslPredicateExecutor<User> {
  Optional<User> findByUsername(String username);

  Optional<Object> findByKakaoId(String kakaoId);
  Optional<User> findByGoogleId(String googleId);
  Optional<User> findByNaverId(String naverId);

  boolean existsByUsername(String username);
}
