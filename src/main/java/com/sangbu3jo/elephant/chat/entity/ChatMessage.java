package com.sangbu3jo.elephant.chat.entity;

import com.sangbu3jo.elephant.chat.dto.ChatMessageRequestDto;
import com.sangbu3jo.elephant.users.entity.User;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.io.Serializable;
import java.time.LocalDateTime;


// mongodb에 저장할 내용
@Document/*(collection = "ele")*/
@Data
@Getter
public class ChatMessage implements Serializable {

    @Id
    @Field(targetType = FieldType.OBJECT_ID)
    private String id;
    private String type; // 메시지 타입
    private Long chatRoomId;
    private User user;
    private String message;
    private LocalDateTime sendTime;

    public ChatMessage(ChatMessageRequestDto chatMessageRequestDto, User user) {
        this.chatRoomId = chatMessageRequestDto.getChatRoomId();
        this.user = user;
        this.message = chatMessageRequestDto.getMessage();
        this.sendTime = chatMessageRequestDto.getSendTime();
        this.type = chatMessageRequestDto.getType().toString();
    }

    public ChatMessage() {}

}
