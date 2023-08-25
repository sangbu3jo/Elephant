package com.sangbu3jo.elephant.cardcomment.repository;

import com.sangbu3jo.elephant.cardcomment.entity.CardComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardcommentRepository extends JpaRepository<CardComment, Long> {
}
