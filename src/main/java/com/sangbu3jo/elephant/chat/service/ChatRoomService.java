package com.sangbu3jo.elephant.chat.service;

import com.sangbu3jo.elephant.board.entity.Board;
import com.sangbu3jo.elephant.chat.dto.ChatMessageRequestDto;
import com.sangbu3jo.elephant.chat.dto.ChatMessageResponseDto;
import com.sangbu3jo.elephant.chat.entity.ChatMessage;
import com.sangbu3jo.elephant.chat.repository.ChatRoomRepository;
import com.sangbu3jo.elephant.chat.entity.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    public void findChatRoom(Board board) {
        Optional<ChatRoom> chatRoom = chatRoomRepository.findById(board.getId());

        if(!chatRoom.isPresent()) {
            ChatRoom newChatRoom =  new ChatRoom(board.getId(), board);
            chatRoomRepository.save(newChatRoom);
        }

    } // findChatRoom

    public void saveChatMessage(ChatMessageRequestDto chatMessageRequestDto) {
        ChatMessage chatMessage = new ChatMessage(chatMessageRequestDto);
    }

//    public List<ChatMessageResponseDto> getMessages(Long chatRoomId) {
//    }

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
