package com.sangbu3jo.elephant.users.repository;

import com.sangbu3jo.elephant.users.custom.UserRepositorySupport;
import com.sangbu3jo.elephant.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long>, UserRepositorySupport /*QuerydslPredicateExecutor<User>*/ {
  Optional<User> findByUsername(String username);

  Optional<Object> findByKakaoId(String kakaoId);
  Optional<User> findByGoogleId(String googleId);
  Optional<User> findByNaverId(String naverId);

  boolean existsByUsername(String username);

  Optional<User> findUserIdByUsername(String username);
}
