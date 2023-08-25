package com.sangbu3jo.elephant.chat;

import com.sangbu3jo.elephant.board.entity.Board;
import com.sangbu3jo.elephant.chat.entity.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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


}
