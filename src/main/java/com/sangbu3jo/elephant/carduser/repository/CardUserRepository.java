package com.sangbu3jo.elephant.carduser.repository;

import com.sangbu3jo.elephant.card.entity.Card;
import com.sangbu3jo.elephant.carduser.entity.CardUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardUserRepository extends JpaRepository<CardUser, Long> {
    List<CardUser> findAllByCard(Card card);
}
