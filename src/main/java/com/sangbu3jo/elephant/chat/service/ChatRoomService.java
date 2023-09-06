package com.sangbu3jo.elephant.chat.service;

import com.sangbu3jo.elephant.board.entity.Board;
import com.sangbu3jo.elephant.board.repository.BoardRepository;
import com.sangbu3jo.elephant.chat.dto.*;
import com.sangbu3jo.elephant.chat.entity.*;
import com.sangbu3jo.elephant.chat.repository.*;
import com.sangbu3jo.elephant.users.entity.User;
import com.sangbu3jo.elephant.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.parameters.P;
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

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatUserRepository chatUserRepository;
    private final BoardRepository boardRepository;
    private final PrivateChatRoomRepository privateChatRoomRepository;
    private final GroupChatRoomRepository groupChatRoomRepository;
    private final GroupChatUserRepository groupChatUserRepository;

    @Autowired
    private final MongoTemplate mongoTemplate;

    /**
     * 프로젝트(보드) 단체 채팅방 정보 찾기
     * @param board: 채팅방 정보를 찾기 위한 프로젝트(보드)
     */
    @Transactional
    public void findChatRoom(Board board) {
        Optional<ChatRoom> chatRoom = chatRoomRepository.findById(board.getId());

        // 단체 채팅방 정보가 없다면 해당 프로젝트의 단체 채팅방 정보를 새로 만들어 저장
        if(!chatRoom.isPresent()) {
            ChatRoom newChatRoom =  new ChatRoom(board.getId(), board);
            ChatRoom savedChatRoom = chatRoomRepository.save(newChatRoom);
            board.updateChatRoom(savedChatRoom);
            boardRepository.save(board);
        }
    }

    /**
     * 프로젝트(보드) 단체 채팅 메세지 저장
     * @param chatMessageRequestDto: 메세지 타입과, 보내는 채팅방의 ID, 유저 정보, 메세지 내용, 보내는 시간을 받아옴
     */
    public void saveChatMessage(ChatMessageRequestDto chatMessageRequestDto) {
        User user = userRepository.findByUsername(chatMessageRequestDto.getUsername()).orElseThrow();
        ChatMessage chatMessage = new ChatMessage(chatMessageRequestDto, user);
        mongoTemplate.save(chatMessage, chatMessageRequestDto.getChatRoomId().toString());
    }

    /**
     * 프로젝트(보드) 단체 채팅방의 메세지 내역을 리스트로
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
     * 사용자가 프로젝트(보드) 단체 채팅방에 참여한 적이 있는 지 확인
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
     * @param user: 개인 채팅방에 참여할 사용자
     * @param users: 개인 채팅방에 참여할 유저들의 List
     * @return: 해당 채팅방으로 이동할 수 있는 URL
     */
    public String findPrivateChatRoom(User user, List<String> users) {
        log.info(user.getUsername());
        users.add(user.getUsername());

        LocalDateTime time = LocalDateTime.now();
        // users의 길이가 > 2 냐 아니냐 (단체냐 개인이냐)
        if (users.size() > 2) {
            // 단체 채팅방은 상관 없이 그냥 만듦 (중복 고려 X)
            GroupChatRoom groupChatRoom = new GroupChatRoom();
            groupChatRoomRepository.save(groupChatRoom);

            // username으로 user를 찾아 GroupChatUser를 생성 후 저장 -> groupChatRoom에 저장
            for (String s: users) {
                log.info(s);
                User findUser = userRepository.findByUsername(s).orElseThrow();
                GroupChatUser groupChatUser = new GroupChatUser(findUser, time, groupChatRoom);
                groupChatUserRepository.save(groupChatUser);
                groupChatRoom.addGroupChatUser(groupChatUser);
            }

            // 만들어진 URL Return
            return "/api/chatRooms/" + groupChatRoom.getTitle();
        } else {
            // 개인 채팅방을 반환해야 함
            String user1 = user.getUsername();
            String user2 = users.get(0);

            Optional<PrivateChatRoom> privateChatRoom = privateChatRoomRepository.findByUser1AndUser2(user1, user2);

            if (privateChatRoom.isPresent()) {
                // 존재한다면 (기존 채팅방으로 이동)
                return "/api/chatRooms/" + privateChatRoom.get().getTitle();
            } else {
                // 존재하지 않는다면 (새로운 채팅방 생성 후 저장 -> 이동)
                PrivateChatRoom pChatRoom = new PrivateChatRoom(user1, user2);
                privateChatRoomRepository.save(pChatRoom);
                return "/api/chatRooms/" + pChatRoom.getTitle();
            }
        }
    }

    /**
     * 개인 채팅방 (단체&개인) 리스트
     * @param user: 개인 채팅방 내역을 확인할 사용자
     * @return: 해당 사용자가 참여하고 있는 개인 채팅방의 리스트를 반환
     */
    public List<PrivateChatRoomResponseDto> findAllPrivateChatRooms(User user) {
        //  privatechatRoom & groupChatRoom 동시에 찾기
        List<GroupChatRoom> chatRooms = groupChatUserRepository.findByUser(user).stream().map(GroupChatUser::getGroupChatRoom).toList();
        List<PrivateChatRoom> privateChatRooms = privateChatRoomRepository.findByUser(user.getUsername());
        List<PrivateChatRoomResponseDto> returnChatRooms = new ArrayList<>();

        Query query = new Query().with(Sort.by(Sort.Order.desc("sendTime"))).limit(1);
        if (chatRooms.size() > 0) {
            for (GroupChatRoom c: chatRooms) {
                // sendTime 필드를 기준으로 최신 아이템 검색
                PrivateChatMessage latestItem = mongoTemplate.findOne(query, PrivateChatMessage.class, c.getTitle());
                returnChatRooms.add(new PrivateChatRoomResponseDto(c, user.getUsername(), latestItem));
            }
        }

        if (privateChatRooms.size() > 0) {
            for (PrivateChatRoom p : privateChatRooms) {
                PrivateChatMessage latestItem = mongoTemplate.findOne(query, PrivateChatMessage.class, p.getTitle());
                returnChatRooms.add(new PrivateChatRoomResponseDto(p, user.getUsername(), latestItem));
            }
        }

        return returnChatRooms;
    }

    /**
     * 개인 채팅방 메세지 저장
     * @param chatMessageRequestDto: 메세지 타입과, 보내는 채팅방의 ID, 유저 정보, 메세지 내용, 보내는 시간을 받아옴
     */
    public void savePrivateChatMessage(PrivateChatMessageRequestDto chatMessageRequestDto) {
        User user = userRepository.findByUsername(chatMessageRequestDto.getUsername()).orElseThrow();
        PrivateChatMessage chatMessage = new PrivateChatMessage(chatMessageRequestDto, user);
        mongoTemplate.save(chatMessage, chatMessageRequestDto.getTitle().toString());
    }

    /**
     * 개인 채팅방 (개인 & 단체) 메세지 데이터를 찾음
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

    /**
     * 개인 채팅방 (개인&단체) 판별
     * @param chatRoomId: 개인 채팅방의 ID 값
     * @return: 1:1이면 false, 그룹 채팅방이면 true
     */
    public Boolean findGroupOrPrivate(String chatRoomId) {
        Optional<PrivateChatRoom> privateChatRoom = privateChatRoomRepository.findByTitle(chatRoomId);
        if (privateChatRoom.isPresent()) {
            return false;
        }
        return true;
    }

    /**
     * 개인 채팅방 (단체만) 떠나기
     * @param user: 떠날 사용자
     * @param chatRoomId: 떠날 채팅방의 ID
     */
    @Transactional
    public void leavePrivateChatRoom(User user, String chatRoomId) {
        GroupChatRoom groupChatRoom = groupChatRoomRepository.findByTitle(chatRoomId);
        Optional<GroupChatUser> groupChatUser = groupChatUserRepository.findByUserAndGroupChatRoom(user, groupChatRoom);

        if (!groupChatUser.isPresent()) {
            throw new IllegalArgumentException();
        }

        // 해당 채팅방에서 유저 삭제
        groupChatRoom.removeGroupChatUser(groupChatUser.get());
        log.info(String.valueOf(groupChatRoom.getGroupChatUsers().size()));
        // 레파지토리에서 유저 삭제
        groupChatUserRepository.delete(groupChatUser.get());

        if (groupChatRoom.getGroupChatUsers().size() == 0) {
            groupChatRoomRepository.delete(groupChatRoom);
        }
    }
}
