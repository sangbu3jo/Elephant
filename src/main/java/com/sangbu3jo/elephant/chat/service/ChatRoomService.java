package com.sangbu3jo.elephant.chat.service;

import com.sangbu3jo.elephant.board.entity.Board;
import com.sangbu3jo.elephant.chat.dto.ChatMessageRequestDto;
import com.sangbu3jo.elephant.chat.dto.ChatMessageResponseDto;
import com.sangbu3jo.elephant.chat.entity.ChatMessage;
import com.sangbu3jo.elephant.chat.repository.ChatRoomRepository;
import com.sangbu3jo.elephant.chat.entity.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    @Autowired
    private final MongoTemplate mongoTemplate;

    public void findChatRoom(Board board) {
        Optional<ChatRoom> chatRoom = chatRoomRepository.findById(board.getId());

        if(!chatRoom.isPresent()) {
            ChatRoom newChatRoom =  new ChatRoom(board.getId(), board);
            chatRoomRepository.save(newChatRoom);
        }

    } // findChatRoom

    public void saveChatMessage(ChatMessageRequestDto chatMessageRequestDto) {
        ChatMessage chatMessage = new ChatMessage(chatMessageRequestDto);
//        chatMessageRepository.save(chatMessage);
        mongoTemplate.save(chatMessage, chatMessageRequestDto.getChatRoomId().toString());
    }

    public List<ChatMessageResponseDto> getMessages(Long chatRoomId) {
        /* 컬렉션 구분이 없으면
            List<ChatMessage> chatMessages = mongoTemplate.find(
                            Query.query(Criteria.where("chatRoomId").is(chatRoomId)),
                            ChatMessage.class
                    );*/

        List<ChatMessage> chatMessages = mongoTemplate.findAll(
                ChatMessage.class,
                chatRoomId.toString()
        );

        return chatMessages.stream().map(ChatMessageResponseDto::new).toList();
    }

    @Transactional
    public Boolean findUsersInChatRoom(String username, Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow();

        if (!chatRoom.getUsers().contains(username)) {
            chatRoom.addUser(username);
            return true; // 포함하고 있지 않다면 (즉 새로 들어온 유저라면)
        }

        return false;
    }

//    public Boolean addUserToChatRoom(Long chatRoomId, String username) {
//        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow();
//
//        if (!chatRoom.getUsers().contains(username)) {
//            chatRoom.addUser(username);
//            return true;
//        }
//        return false;
//    }
}
