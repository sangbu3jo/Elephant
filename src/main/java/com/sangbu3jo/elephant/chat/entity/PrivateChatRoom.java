package com.sangbu3jo.elephant.chat.entity;

import jakarta.persistence.Id;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.util.UUID;

@Document(collection = "privatechatroom")
@Getter
public class PrivateChatRoom {

    // 기본으로 주는 Object_ID를 사용할 예정
    @Id
    @Field(targetType = FieldType.OBJECT_ID)
    private String id;

    private String firstUser;

    private String secondUser;

    private String title; // UUID로 구분할 예정

    public PrivateChatRoom(String firstUser, String secondUser) {
        this.firstUser = firstUser;
        this.secondUser = secondUser;
        this.title = UUID.randomUUID().toString();
    }


}
