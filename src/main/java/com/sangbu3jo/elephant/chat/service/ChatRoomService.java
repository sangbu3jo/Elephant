package com.sangbu3jo.elephant.chat.service;

import com.sangbu3jo.elephant.board.entity.Board;
import com.sangbu3jo.elephant.chat.dto.*;
import com.sangbu3jo.elephant.chat.entity.*;
import com.sangbu3jo.elephant.chat.repository.ChatRoomRepository;
import com.sangbu3jo.elephant.chat.repository.ChatUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatUserRepository chatUserRepository;

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
        mongoTemplate.save(chatMessage, chatMessageRequestDto.getChatRoomId().toString());
    }

    // 채팅 메시지 (단체 채팅방)
    public List<ChatMessageResponseDto> getMessages(Long chatRoomId, String username) {
        /* 컬렉션 구분이 없으면
            List<ChatMessage> chatMessages = mongoTemplate.find(
                            Query.query(Criteria.where("chatRoomId").is(chatRoomId)),
                            ChatMessage.class
                    );*/

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow();
        ChatUser chatUser = chatUserRepository.findByUsernameAndChatroom(username, chatRoom).orElseThrow(); // chatuser가 있으면 찾아주는 걸로 하자
        LocalDateTime time = chatUser.getEnterTime();
        log.info(time.toString());

        Query query = new Query();
        Criteria criteria = Criteria.where("sendTime").gte(time);
        query.addCriteria(criteria);

        // 이제 저 시간 이후의 데이터만 갖고 오는 것으로 조건 추가
        List<ChatMessage> chatMessages = mongoTemplate.find(
                query,
                ChatMessage.class,
                chatRoomId.toString()
        );

//        List<ChatMessage> chatMessages = mongoTemplate.findAll(
//                ChatMessage.class,
//                chatRoomId.toString()
//        );

        return chatMessages.stream().map(ChatMessageResponseDto::new).toList();
    }

    @Transactional
    public Boolean findUsersInChatRoom(String username, Long chatRoomId, LocalDateTime time) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow();
        Optional<ChatUser> chatUser = chatUserRepository.findByUsernameAndChatroom(username, chatRoom);
        if (!chatUser.isPresent()) {
            ChatUser newChatUser = new ChatUser(username, time);
            chatRoom.addUser(newChatUser);
            chatUserRepository.save(newChatUser);
            return true;
        }
        return false;
    }

    public String findPrivateChatRoom(String firstUser, String secondUser) {
        log.info(firstUser);
        log.info(secondUser);

        Query query = new Query();
        Criteria criteria = new Criteria();

        // user1이 user1혹은 user2, user2가 user1 혹은 user2 인 경우
        criteria.orOperator(
                Criteria.where("firstUser").is(firstUser).and("secondUser").is(secondUser),
                Criteria.where("firstUser").is(secondUser).and("secondUser").is(firstUser)
        );

        query.addCriteria(criteria);

        // 해당 쿼리에 해당하는 개인 채팅방을 찾아옴
        PrivateChatRoom chatRoom = mongoTemplate.findOne(query, PrivateChatRoom.class);

        if (chatRoom == null) {
            PrivateChatRoom newChatRoom =  new PrivateChatRoom(firstUser, secondUser);
            PrivateChatRoom saveChatRoom = mongoTemplate.save(newChatRoom);
            return "redirect:/api/chatRooms/" + saveChatRoom.getTitle();
        }

        return "redirect:/api/chatRooms/" + chatRoom.getTitle();
    }

    public List<PrivateChatRoomResponseDto> findAllPrivateChatRooms(String username) {
        // username도 같이 넣어주어야 하는데 ,, 이걸 우째 stream으로 한번에 못하나?
        Criteria criteria = new Criteria().orOperator(
                Criteria.where("firstUser").is(username),
                Criteria.where("secondUser").is(username)
        );

        Query query = new Query(criteria);

        List<PrivateChatRoom> chatRooms = mongoTemplate.find(query, PrivateChatRoom.class, "privatechatroom");
        List<PrivateChatRoomResponseDto> returnChatRooms = new ArrayList<>();
        Query query1 = new Query().with(Sort.by(Sort.Order.desc("sendTime"))).limit(1);
        for (PrivateChatRoom c: chatRooms) {
             // sendTime 필드를 기준으로 최신 아이템 검색
            PrivateChatMessage latestItem = mongoTemplate.findOne(query1, PrivateChatMessage.class, c.getTitle());
            returnChatRooms.add(new PrivateChatRoomResponseDto(c, username, latestItem));
        }
        return returnChatRooms;
    }

    public void savePrivateChatMessage(PrivateChatMessageRequestDto chatMessageRequestDto) {
        PrivateChatMessage chatMessage = new PrivateChatMessage(chatMessageRequestDto);
//        chatMessageRepository.save(chatMessage);
        mongoTemplate.save(chatMessage, chatMessageRequestDto.getTitle().toString());
    }

    public List<PrivateChatMessageResponseDto> getPrivateMessages(String chatRoomId) {

        List<PrivateChatMessage> chatMessages = mongoTemplate.findAll(
                PrivateChatMessage.class,
                chatRoomId
        );

        return chatMessages.stream().map(PrivateChatMessageResponseDto::new).toList();
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
