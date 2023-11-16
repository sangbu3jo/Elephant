package com.sangbu3jo.elephant.chat.service;

import com.sangbu3jo.elephant.chat.dto.GroupChatUserResponseDto;
import com.sangbu3jo.elephant.chat.dto.PrivateChatMessageRequestDto;
import com.sangbu3jo.elephant.chat.dto.PrivateChatMessageResponseDto;
import com.sangbu3jo.elephant.chat.dto.PrivateChatRoomResponseDto;
import com.sangbu3jo.elephant.chat.entity.GroupChatRoom;
import com.sangbu3jo.elephant.chat.entity.GroupChatUser;
import com.sangbu3jo.elephant.chat.entity.PrivateChatMessage;
import com.sangbu3jo.elephant.chat.entity.PrivateChatRoom;
import com.sangbu3jo.elephant.chat.repository.GroupChatRoomRepository;
import com.sangbu3jo.elephant.chat.repository.GroupChatUserRepository;
import com.sangbu3jo.elephant.chat.repository.PrivateChatRoomRepository;
import com.sangbu3jo.elephant.notification.service.NotificationService;
import com.sangbu3jo.elephant.users.entity.User;
import com.sangbu3jo.elephant.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
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
public class ChatService {

    private final UserRepository userRepository;
    private final PrivateChatRoomRepository privateChatRoomRepository;
    private final GroupChatRoomRepository groupChatRoomRepository;
    private final GroupChatUserRepository groupChatUserRepository;
    private final NotificationService notificationService;

    @Autowired
    private final MongoTemplate mongoTemplate;

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
        // 사용자를 찾음
        User user = userRepository.findByUsername(chatMessageRequestDto.getUsername()).orElseThrow();

        // 채팅 메세지 저장
        PrivateChatMessage chatMessage = new PrivateChatMessage(chatMessageRequestDto);
        mongoTemplate.save(chatMessage, chatMessageRequestDto.getTitle().toString());

        // 채팅 메세지 반환
        PrivateChatMessageResponseDto message = new PrivateChatMessageResponseDto(chatMessage, user);

        sendAlert(chatMessageRequestDto.getTitle(), user);

        return message;
    }

    /**
     * 개인 채팅방 (개인 & 단체) 메세지 데이터를 찾음
     * @param chatRoomId: 개인 채팅방의 ID 값
     * @return: 개인 채팅방에 존재하는 메세지 데이터를 찾아 리스트로 반환
     */
    public List<PrivateChatMessageResponseDto> getPrivateMessages(String chatRoomId, Integer pageNo) {
        int skip = pageNo * 15; // 건너뛸 문서 수 계산
        Query query = new Query()
                .with(Sort.by(Sort.Direction.DESC, "sendTime")).skip(skip).limit(15);
        List<PrivateChatMessage> chatMessagesList = mongoTemplate.find(query, PrivateChatMessage.class, chatRoomId);

        List<PrivateChatMessageResponseDto> messages = new ArrayList<>();

        for (PrivateChatMessage chatMessage : chatMessagesList) {
            // 사용자 정보 가져오기
            User user = userRepository.findByUsername(chatMessage.getUsername()).orElse(null);

            if (user != null) {
                // user 정보를 PrivateChatMessageResponseDto에 추가
                PrivateChatMessageResponseDto responseDto = new PrivateChatMessageResponseDto(chatMessage, user);
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
            if (users.size() > 0) {
                for (GroupChatUser groupChatUser : users) {
                    if (!title.isEmpty()) {
                        title += ", ";
                    }

                    if (!groupChatUser.getUser().getUsername().equals(username)) {
                        title += groupChatUser.getUser().getUsername();
                    }

                    if (title.length() > 40) {
                        break;
                    }
                }

                if (title.length() > 40) {
                    model.addAttribute("title",  title + "...");
                } else {
                    model.addAttribute("title",  title);
                }
                model.addAttribute("group", true);
                model.addAttribute("users", findUsers(groupChatRoom));
            }
        }
    }

    /**
     * 단체 채팅방 (1:1 X) 떠나기
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
     * 단체 채팅방의 경우 참여하고 있는 사용자의 목록을 반환
     * @param groupChatRoom: 그룹 채팅방
     * @return: 해당 채팅방에 참여하고 있는 사용자들의 List
     */
    public List<GroupChatUserResponseDto> findUsers(GroupChatRoom groupChatRoom) {
        return groupChatUserRepository.findByGroupChatRoom(groupChatRoom).stream().map(GroupChatUserResponseDto::new).toList();
    }

    /**
     * 메세지 알림 전송
     * @param chatTitle: 채팅방의 이름
     * @param user: 메세지를 보낸 사용자
     */
    public void sendAlert(String chatTitle, User user) {
        Optional<String> user1 = privateChatRoomRepository.findUser1ByTitle(chatTitle);
        Optional<String> user2 = privateChatRoomRepository.findUser2ByTitle(chatTitle);

        // 1:1 개인 채팅방
        if (user1.isPresent() && user2.isPresent()) {
            String username1 = user1.get();
            String username2 = user2.get();

            // 현재 사용자와 user1, user2를 비교하여 알림을 보냅니다.
            if (!user.getUsername().equals(user1)) {
                User findUser1 = userRepository.findUserIdByUsername(username1).orElseThrow(
                        () -> new IllegalArgumentException("존재하지 않는 유저입니다.") );
                String notificationContent = user.getNickname() + "님이 메시지를 보냈습니다.";
                String notificationUrl = "/api/chatRooms/" + chatTitle;
                notificationService.oneMessgeNotification(findUser1.getId(), notificationContent, notificationUrl);
            } else {
                User findUser2 = userRepository.findUserIdByUsername(username2).orElseThrow(
                        () -> new IllegalArgumentException("존재하지 않는 유저입니다.") );
                String notificationContent = user.getNickname() + "님이 메시지를 보냈습니다.";
                String notificationUrl = "/api/chatRooms/" + chatTitle;
                notificationService.oneMessgeNotification(findUser2.getId(), notificationContent, notificationUrl);
            }

        // 단체 채팅방
        } else {
            // 단체 채팅 알림을 보내는 로직 추가
            GroupChatRoom groupChatRoom = groupChatRoomRepository.findByTitle(chatTitle);
            List<GroupChatUser> groupChatUsers = groupChatUserRepository.findByGroupChatRoom(groupChatRoom);
            for (GroupChatUser groupChatUser : groupChatUsers) {
                User targetUser = groupChatUser.getUser();

                // 메시지를 보낸유저와 단체 채팅방에 포함된 유저가 일치하지 않을시 알림 전송
                if (!user.getUsername().equals(targetUser.getUsername())) {
                    String notificationContent = user.getNickname() + "님이 메시지를 보냈습니다.";
                    String notificationUrl = "/api/chatRooms/" + chatTitle;
                    notificationService.manyMessgeNotification(targetUser.getId(), notificationContent, notificationUrl);
                }
            }
        }
    }
}
