package com.sangbu3jo.elephant.chat.entity;

import com.sangbu3jo.elephant.chat.dto.ChatMessageRequestDto;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.Getter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.io.Serializable;


// mongodb에 저장할 내용
@Document(collection = "ele")
@Data
@Getter
public class ChatMessage implements Serializable {

    @Id
    @Field(targetType = FieldType.OBJECT_ID)
    private String id;

    private String type; // 메시지 타입
    private Long chatRoomId;
    private String nickname; // 클라이언트에서 표시용
    private String username; // 서버와 통신용
    private String message;
    private String sendTime;

    public ChatMessage(ChatMessageRequestDto chatMessageRequestDto) {
        this.chatRoomId = chatMessageRequestDto.getChatRoomId();
        this.nickname = chatMessageRequestDto.getNickname();
        this.username = chatMessageRequestDto.getUsername();
        this.message = chatMessageRequestDto.getMessage();
        this.sendTime = chatMessageRequestDto.getSendTime().toString();
        this.type = chatMessageRequestDto.getType().toString();
    }

    public ChatMessage() {}

}
