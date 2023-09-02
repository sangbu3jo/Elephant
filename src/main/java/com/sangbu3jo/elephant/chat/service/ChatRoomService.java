package com.sangbu3jo.elephant.chat.service;

import com.sangbu3jo.elephant.board.entity.Board;
import com.sangbu3jo.elephant.board.repository.BoardRepository;
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
    private final BoardRepository boardRepository;

    @Autowired
    private final MongoTemplate mongoTemplate;

    /**
     * 단체 채팅방 정보 찾기
     * @param board: 채팅방 정보를 찾기 위한 프로젝트(보드)
     */
    public void findChatRoom(Board board) {
        Optional<ChatRoom> chatRoom = chatRoomRepository.findById(board.getId());

        // 단체 채팅방 정보가 없다면 해당 프로젝트의 단체 채팅방 정보를 새로 만들어 저장
        if(!chatRoom.isPresent()) {
            ChatRoom newChatRoom =  new ChatRoom(board.getId(), board);
            chatRoomRepository.save(newChatRoom);
            board.updateChatRoom(newChatRoom);
            boardRepository.save(board);
        }

    }

    /**
     * 단체 채팅 메세지 저장
     * @param chatMessageRequestDto: 메세지 타입과, 보내는 채팅방의 ID, 유저 정보, 메세지 내용, 보내는 시간을 받아옴
     */
    public void saveChatMessage(ChatMessageRequestDto chatMessageRequestDto) {
        ChatMessage chatMessage = new ChatMessage(chatMessageRequestDto);
        mongoTemplate.save(chatMessage, chatMessageRequestDto.getChatRoomId().toString());
    }

    /**
     * 단체 채팅방의 메세지 내역을 리스트로
     * @param chatRoomId: 단체 채팅방의 ID 값
     * @param username: 단체 채탕방에 참여하는 사용자의 이름(username)
     * @return: 단체 채팅방의 메세지 데이터의 리스트를 반환
     */
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

        // 유저의 입장 시간 이후의 데이터만 갖고 오는 것으로 조건 추가
        List<ChatMessage> chatMessages = mongoTemplate.find(
                query,
                ChatMessage.class,
                chatRoomId.toString()
        );

        return chatMessages.stream().map(ChatMessageResponseDto::new).toList();
    }

    /**
     * 사용자가 단체 채팅방에 참여한 적이 있는 지 확인
     * @param username: 단체 채팅방에 참여할 사용자의 이름(username)
     * @param chatRoomId: 단체 채팅방의 ID 값
     * @param time: 단체 채팅방에 접속한 시간
     * @return: 단체 채팅방에 처음 참여했다면 true, 참여한 기록이 있다면 false를 반환
     */
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

    /**
     * 개인 채팅방 URL 반환
     * @param firstUser: 개인 채팅방에 참여할 사용자의 이름(username) 1
     * @param secondUser: 개인 채팅방에 참여할 사용자의 이름(username) 2
     * @return: 해당 채팅방으로 이동할 수 있는 URL
     */
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
            return "/api/chatRooms/" + saveChatRoom.getTitle();
        }

        return "/api/chatRooms/" + chatRoom.getTitle();
    }

    /**
     * 개인 채팅방 리스트
     * @param username: 개인 채팅방 내역을 확인할 사용자의 이름(username)
     * @return: 해당 사용자가 참여하고 있는 개인 채팅방의 리스트를 반환
     */
    public List<PrivateChatRoomResponseDto> findAllPrivateChatRooms(String username) {
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

    /**
     * 개인 채팅방 메세지 저장
     * @param chatMessageRequestDto: 메세지 타입과, 보내는 채팅방의 ID, 유저 정보, 메세지 내용, 보내는 시간을 받아옴
     */
    public void savePrivateChatMessage(PrivateChatMessageRequestDto chatMessageRequestDto) {
        PrivateChatMessage chatMessage = new PrivateChatMessage(chatMessageRequestDto);
        mongoTemplate.save(chatMessage, chatMessageRequestDto.getTitle().toString());
    }

    /**
     * 개인 채팅방 메세지 데이터를 찾음
     * @param chatRoomId: 개인 채팅방의 ID 값
     * @return: 개인 채팅방에 존재하는 메세지 데이터를 찾아 리스트로 반환
     */
    public List<PrivateChatMessageResponseDto> getPrivateMessages(String chatRoomId) {

        List<PrivateChatMessage> chatMessages = mongoTemplate.findAll(
                PrivateChatMessage.class,
                chatRoomId
        );

        return chatMessages.stream().map(PrivateChatMessageResponseDto::new).toList();
    }

}
