package com.sangbu3jo.elephant.chat.service;

import com.sangbu3jo.elephant.board.entity.Board;
import com.sangbu3jo.elephant.board.repository.BoardRepository;
import com.sangbu3jo.elephant.chat.dto.ChatMessageRequestDto;
import com.sangbu3jo.elephant.chat.dto.ChatMessageResponseDto;
import com.sangbu3jo.elephant.chat.entity.ChatMessage;
import com.sangbu3jo.elephant.chat.entity.ChatRoom;
import com.sangbu3jo.elephant.chat.entity.ChatUser;
import com.sangbu3jo.elephant.chat.repository.ChatRoomRepository;
import com.sangbu3jo.elephant.chat.repository.ChatUserRepository;
import com.sangbu3jo.elephant.notification.service.NotificationService;
import com.sangbu3jo.elephant.users.entity.User;
import com.sangbu3jo.elephant.users.repository.UserRepository;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectChatService {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatUserRepository chatUserRepository;
    private final BoardRepository boardRepository;
    private final NotificationService notificationService;

    @Autowired
    private final MongoTemplate mongoTemplate;

    /**
     * 프로젝트(보드) 단체 채팅방 정보 찾기
     * @param board: 채팅방 정보를 찾기 위한 프로젝트(보드)
     */
    @Transactional
    public String findChatRoom(Board board) {
        Optional<ChatRoom> chatRoom = chatRoomRepository.findByRoomId(board.getId());

        // 단체 채팅방 정보가 없다면 해당 프로젝트의 단체 채팅방 정보를 새로 만들어 저장
        if(!chatRoom.isPresent()) {
            ChatRoom newChatRoom =  new ChatRoom(board.getId(), board);
            ChatRoom savedChatRoom = chatRoomRepository.save(newChatRoom);
            board.updateChatRoom(savedChatRoom);
            boardRepository.save(board);
        }

        return board.getTitle();
    }


    /**
     * 프로젝트(보드) 단체 채팅 메세지 저장
     * @param chatMessageRequestDto: 메세지 타입과, 보내는 채팅방의 ID, 유저 정보, 메세지 내용, 보내는 시간을 받아옴
     * @return: 메세지 내용을 dto에 담아 반환
     */
    public ChatMessageResponseDto saveChatMessage(ChatMessageRequestDto chatMessageRequestDto) {
        // 채팅 메세지를 보내는 해당 유저를 찾음
        User user = userRepository.findByUsername(chatMessageRequestDto.getUsername()).orElseThrow();
        
        // 채팅 메세지 저장
        ChatMessage chatMessage = new ChatMessage(chatMessageRequestDto);
        mongoTemplate.save(chatMessage, chatMessageRequestDto.getChatRoomId().toString());
        
        // 채팅 메세지 반환
        ChatMessageResponseDto message = new ChatMessageResponseDto(chatMessage, user);

        // 사용자들에게 알림 전송
        sendAlert(user, chatMessageRequestDto);

        return message;
    }

    /**
     * 프로젝트(보드) 단체 채팅방의 메세지 내역을 리스트로
     * @param chatRoomId: 단체 채팅방의 ID 값
     * @param username: 단체 채탕방에 참여하는 사용자의 이름(username)
     * @return: 단체 채팅방의 메세지 데이터의 리스트를 반환
     */
    public List<ChatMessageResponseDto> getMessages(Long chatRoomId, String username, Integer pageNo) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(chatRoomId).orElseThrow();

        Optional<ChatUser> chatUserOptional = chatUserRepository.findByUsernameAndChatroom(username, chatRoom); // chatuser가 있으면 찾아주는 걸로 하자

        if (!chatUserOptional.isPresent()) {
            throw new IllegalArgumentException("존재하지 않습니다.");
        }

        LocalDateTime time = chatUserOptional.get().getEnterTime();

        int skip = pageNo * 15; // 건너뛸 문서 수 계산
        Query query = new Query()
                .with(Sort.by(Sort.Direction.DESC, "sendTime")).skip(skip).limit(15);
        Criteria criteria = Criteria.where("sendTime").gte(time);
        query.addCriteria(criteria);

        // 유저의 입장 시간 이후의 데이터만 갖고 오는 것으로 조건 추가
        List<ChatMessage> chatMessages = mongoTemplate.find(
                query,
                ChatMessage.class,
                chatRoomId.toString()
        );

        return chatMessageResponseDtos(chatMessages);
    }


    /**
     * 사용자가 프로젝트(보드) 단체 채팅방에 참여한 적이 있는 지 확인
     * @param username: 단체 채팅방에 참여할 사용자의 이름(username)
     * @param chatRoomId: 단체 채팅방의 ID 값
     * @param time: 단체 채팅방에 접속한 시간
     * @return: 단체 채팅방에 처음 참여했다면 true, 참여한 기록이 있다면 false를 반환
     */
    @Transactional
    public Boolean findUsersInChatRoom(String username, Long chatRoomId, LocalDateTime time) {
        // 채팅 방 확인
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(chatRoomId).orElseThrow();

        // 채팅 유저 확인
        Optional<ChatUser> chatUser = chatUserRepository.findByUsernameAndChatroom(username, chatRoom);

        if (!chatUser.isPresent()) {
            ChatUser newChatUser = new ChatUser(username, time);
            chatRoom.addUser(newChatUser);
            chatUserRepository.save(newChatUser);
            return true; // 참여 이력 X (처음 참여)
        }
        return false;    // 참여 이력 O (이전에 참여)
    }

    /**
     * 채팅 메세지(엔티티)를 반환할 형태로 변환
     * @param chatMessages: 채팅 메세지 리스트
     * @return: 채팅 메세지들을 반환
     */
    public List<ChatMessageResponseDto> chatMessageResponseDtos(List<ChatMessage> chatMessages) {
        // 빈 리스트 생성
        List<ChatMessageResponseDto> messages = new ArrayList<>();

        // 받아온 메시지를 반환 메세지 형태로 변환
        for (ChatMessage chatMessage : chatMessages) {
            // userRepository에서 유저 정보 가져오기
            User user = userRepository.findByUsername(chatMessage.getUsername()).orElse(null);
            // 사용자가 존재하는 경우
            if (user != null) {
                // user 정보를 PrivateChatMessageResponseDto에 추가
                ChatMessageResponseDto responseDto = new ChatMessageResponseDto(chatMessage, user);
                messages.add(responseDto);
            }
        }
        return messages;
    }

    /**
     * 채팅방 참여자들에게 알림 전송
     * @param user: 채팅을 보낸 사람
     * @param chatMessageRequestDto: 채팅 메세지 요청 DTO
     */
    public void sendAlert(User user, ChatMessageRequestDto chatMessageRequestDto) {
        // 방 번호를 기준으로 해당 방에 속한 유저 리스트를 가져옴
        List<ChatUser> users = chatUserRepository.findByChatroom_RoomId(chatMessageRequestDto.getChatRoomId());

        // 메시지를 보낸 유저를 제외한 나머지 유저에게 알림을 보냄
        if (!users.isEmpty()) {
            for (ChatUser chatUser : users) {
                if (!chatUser.getUsername().equals(user.getUsername())) {
                    User chatUserF = userRepository.findUserIdByUsername(chatUser.getUsername()).orElseThrow(
                            () -> new IllegalArgumentException("사용자가 존재하지 않습니다.")
                    );
                    String notificationContent = user.getNickname() + "님이 메시지를 보냈습니다.";
                    String notificationUrl = "/api/chat/" + chatMessageRequestDto.getChatRoomId().toString();
                    notificationService.projectMessgeNotification(chatUserF.getId(), notificationContent, notificationUrl);

                }
            }
        }
    }

}
