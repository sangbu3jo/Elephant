package com.sangbu3jo.elephant.chat.service;

import com.sangbu3jo.elephant.board.entity.Board;
import com.sangbu3jo.elephant.board.repository.BoardRepository;
import com.sangbu3jo.elephant.chat.dto.*;
import com.sangbu3jo.elephant.chat.entity.*;
import com.sangbu3jo.elephant.chat.repository.*;
import com.sangbu3jo.elephant.notification.service.NotificationService;
import com.sangbu3jo.elephant.users.entity.User;
import com.sangbu3jo.elephant.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

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
    private final NotificationService notificationService;

    @Autowired
    private final MongoTemplate mongoTemplate;

    /**
     * 프로젝트(보드) 단체 채팅방 정보 찾기
     * @param board: 채팅방 정보를 찾기 위한 프로젝트(보드)
     */
    @Transactional
    public void findChatRoom(Board board) {
        Optional<ChatRoom> chatRoom = chatRoomRepository.findByRoomId(board.getId());

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
     * @return: 메세지 내용을 dto에 담아 반환
     */
    public ChatMessageResponseDto saveChatMessage(ChatMessageRequestDto chatMessageRequestDto) {
        User user = userRepository.findByUsername(chatMessageRequestDto.getUsername()).orElseThrow();
        ChatMessage chatMessage = new ChatMessage(chatMessageRequestDto, user);
        mongoTemplate.save(chatMessage, chatMessageRequestDto.getChatRoomId().toString());
        ChatMessageResponseDto message = new ChatMessageResponseDto(chatMessage);
        message.updateUrl(user.getProfileUrl());

        log.info("방 번호" + message.getChatRoomId());

        // 방 번호를 기준으로 해당 방에 속한 유저 리스트를 가져옴
        List<ChatUser> users = chatUserRepository.findByChatroom_RoomId(chatMessageRequestDto.getChatRoomId());

        // 메시지를 보낸 유저를 제외한 나머지 유저에게 알림을 보냄
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


        return message;
    }

    /**
     * 프로젝트(보드) 단체 채팅방의 메세지 내역을 리스트로
     * @param chatRoomId: 단체 채팅방의 ID 값
     * @param username: 단체 채탕방에 참여하는 사용자의 이름(username)
     * @return: 단체 채팅방의 메세지 데이터의 리스트를 반환
     */
    public List<ChatMessageResponseDto> getMessages(Long chatRoomId, String username, Integer pageNo) {
        /* 컬렉션 구분이 없으면
            List<ChatMessage> chatMessages = mongoTemplate.find(
                            Query.query(Criteria.where("chatRoomId").is(chatRoomId)),
                            ChatMessage.class
                    );*/

        ChatRoom chatRoom = chatRoomRepository.findByRoomId(chatRoomId).orElseThrow();

        Optional<ChatUser> chatUserOptional = chatUserRepository.findByUsernameAndChatroom(username, chatRoom); // chatuser가 있으면 찾아주는 걸로 하자

        if (!chatUserOptional.isPresent()) {
            throw new IllegalArgumentException("존재하지 않습니다.");
        }

        LocalDateTime time = chatUserOptional.get().getEnterTime();
        log.info(time.toString());

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

        List<ChatMessageResponseDto> messages = new ArrayList<>();

        for (ChatMessage chatMessage : chatMessages) {
            // userRepository를 사용하여 user 정보 가져오기
            User user = userRepository.findById(chatMessage.getUser().getId()).orElse(null);

            if (user != null) {
                // user 정보를 PrivateChatMessageResponseDto에 추가
                ChatMessageResponseDto responseDto = new ChatMessageResponseDto(chatMessage);
                responseDto.updateUrl(user.getProfileUrl());
                messages.add(responseDto);
            }
        }

        return messages;
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
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(chatRoomId).orElseThrow();
        Optional<ChatUser> chatUser = chatUserRepository.findByUsernameAndChatroom(username, chatRoom);
        if (!chatUser.isPresent()) {
            log.info("유저 존재 X");
            ChatUser newChatUser = new ChatUser(username, time);
            chatRoom.addUser(newChatUser);
            chatUserRepository.save(newChatUser);
            return true;
        }
        log.info("유저 존재");
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
     * @return: ResponseDto에 담아서 반환
     */
    public PrivateChatMessageResponseDto savePrivateChatMessage(PrivateChatMessageRequestDto chatMessageRequestDto) {
        User user = userRepository.findByUsername(chatMessageRequestDto.getUsername()).orElseThrow();
        PrivateChatMessage chatMessage = new PrivateChatMessage(chatMessageRequestDto, user);
        mongoTemplate.save(chatMessage, chatMessageRequestDto.getTitle().toString());
        PrivateChatMessageResponseDto message = new PrivateChatMessageResponseDto(chatMessage);
        message.updateUrl(user.getProfileUrl());

        Optional<String> user1Optional = privateChatRoomRepository.findUser1ByTitle(message.getTitle());
        Optional<String> user2Optional = privateChatRoomRepository.findUser2ByTitle(message.getTitle());

        if (user1Optional.isPresent() && user2Optional.isPresent()) {
            String user1 = user1Optional.get();
            String user2 = user2Optional.get();

            // 현재 사용자와 user1, user2를 비교하여 알림을 보냅니다.
            if (!user.getUsername().equals(user1)) {
                User user1Hello = userRepository.findUserIdByUsername(user1).orElseThrow(
                        () -> new IllegalArgumentException("존재하지 않는 유저입니다.")
                );
                String notificationContent = user.getNickname() + "님이 메시지를 보냈습니다.";
                String notificationUrl = "/api/chatRooms/" + chatMessageRequestDto.getTitle().toString();
                notificationService.oneMessgeNotification(user1Hello.getId(), notificationContent, notificationUrl);
            }

            if (!user.getUsername().equals(user2)) {
                User user2Hello = userRepository.findUserIdByUsername(user2).orElseThrow(
                        () -> new IllegalArgumentException("존재하지 않는 유저입니다.")
                );
                String notificationContent = user.getNickname() + "님이 메시지를 보냈습니다.";
                String notificationUrl = "/api/chatRooms/" + chatMessageRequestDto.getTitle().toString();
                notificationService.oneMessgeNotification(user2Hello.getId(), notificationContent, notificationUrl);
            }

        } else {
            // 단체 채팅 알림을 보내는 로직 추가
            log.info("개인 채팅방 저장 메서드 else시 들어오는 로그 " + message.getTitle());

            GroupChatRoom groupChatRoom = groupChatRoomRepository.findByTitle(message.getTitle());
            List<GroupChatUser> groupChatUsers = groupChatUserRepository.findByGroupChatRoom(groupChatRoom);
            for (GroupChatUser groupChatUser : groupChatUsers) {
                User targetUser = groupChatUser.getUser();

                // 메시지를 보낸유저와 단체 채팅방에 포함된 유저가 일치하지 않을시 알림 전송
                if (!user.getUsername().equals(targetUser.getUsername())) {
                    String notificationContent = user.getNickname() + "님이 메시지를 보냈습니다.";
                    String notificationUrl = "/api/chatRooms/" + chatMessageRequestDto.getTitle().toString();
                    notificationService.manyMessgeNotification(targetUser.getId(), notificationContent, notificationUrl);
                }
            }
        }


        log.info("개인 채팅방 저장 메서드" + message.getTitle());

        return message;
    }

    /**
     * 개인 채팅방 (개인 & 단체) 메세지 데이터를 찾음
     * @param chatRoomId: 개인 채팅방의 ID 값
     * @return: 개인 채팅방에 존재하는 메세지 데이터를 찾아 리스트로 반환
     */
    public List<PrivateChatMessageResponseDto> getPrivateMessages(String chatRoomId, Integer pageNo) {

//        List<PrivateChatMessage> chatMessages = mongoTemplate.findAll(
//                PrivateChatMessage.class,
//                chatRoomId
//        );

        int skip = pageNo * 15; // 건너뛸 문서 수 계산
        Query query = new Query()
                .with(Sort.by(Sort.Direction.DESC, "sendTime")).skip(skip).limit(15);
        List<PrivateChatMessage> chatMessagesList = mongoTemplate.find(query, PrivateChatMessage.class, chatRoomId);

        List<PrivateChatMessageResponseDto> messages = new ArrayList<>();

        for (PrivateChatMessage chatMessage : chatMessagesList) {
            // userRepository를 사용하여 user 정보 가져오기
            User user = userRepository.findById(chatMessage.getUser().getId()).orElse(null);

            if (user != null) {
                // user 정보를 PrivateChatMessageResponseDto에 추가
                PrivateChatMessageResponseDto responseDto = new PrivateChatMessageResponseDto(chatMessage);
                responseDto.updateUrl(user.getProfileUrl());
                messages.add(responseDto);
            }

        }
        return messages;
    }

    /**
     * 개인 채팅방 (개인&단체) 이름
     * @param chatRoomId: 개인 채팅방의 ID 값
     * @return: 1:1이면 상대의 username, 그룹 채팅방이면 내 이름을 제외한 다른 이들의 username (40자까지만)
     */
    public void findGroupOrPrivate(String chatRoomId, String username, Model model) {
        Optional<PrivateChatRoom> privateChatRoom = privateChatRoomRepository.findByTitle(chatRoomId);
        if (privateChatRoom.isPresent()) {
            if (privateChatRoom.get().getUser1().equals(username)) {
                model.addAttribute("title",  privateChatRoom.get().getUser2());
            } else {
                model.addAttribute("title",  privateChatRoom.get().getUser1());
            }
            model.addAttribute("group", false);
        } else {
            GroupChatRoom groupChatRoom = groupChatRoomRepository.findByTitle(chatRoomId);
            List<GroupChatUser> users = groupChatRoom.getGroupChatUsers();
            String title = "";
            for (int i = 0; i < users.size(); i++) {
                GroupChatUser g = users.get(i);
                if (g.getUser().getUsername().equals(username)) {
                    continue;
                }

                if (!title.isEmpty()) {
                    title += ", ";
                }

                title += g.getUser().getUsername();

                if (title.length() > 40) {
                    break;
                }
            }
            if (title.length() > 40) {
                model.addAttribute("title",  title.substring(0, 40) + "...");
            } else {
                model.addAttribute("title",  title);
            }
            model.addAttribute("group", true);
            model.addAttribute("users", findUsers(chatRoomId));
        }


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

    /**
     * 단체 채팅방의 경우만 참여하고 있는 사용자의 목록을 반환
     * @param chatRoomId: 채팅방의 Title
     * @return: 해당 채팅방에 참여하고 있는 사용자들의 List
     */
    public List<GroupChatUserResponseDto> findUsers(String chatRoomId) {
        GroupChatRoom groupChatRoom = groupChatRoomRepository.findByTitle(chatRoomId);
        return groupChatUserRepository.findByGroupChatRoom(groupChatRoom).stream().map(GroupChatUserResponseDto::new).toList();
    }
}
