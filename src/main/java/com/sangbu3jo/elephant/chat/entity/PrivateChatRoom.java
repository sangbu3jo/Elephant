package com.sangbu3jo.elephant.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PrivateChatRoom {

    // 기본으로 주는 Object_ID를 사용할 예정
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String user1;
    private String user2;

    private String title; // UUID로 구분할 예정

    public PrivateChatRoom(String user1, String user2) {
        this.user1 = user1;
        this.user2 = user2;
        this.title = UUID.randomUUID().toString();
    }

}
