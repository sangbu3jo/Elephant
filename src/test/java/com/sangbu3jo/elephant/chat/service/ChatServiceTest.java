package com.sangbu3jo.elephant.chat.service;

import com.sangbu3jo.elephant.auth.dto.SignupRequestDto;
import com.sangbu3jo.elephant.chat.dto.MessageType;
import com.sangbu3jo.elephant.chat.dto.PrivateChatMessageRequestDto;
import com.sangbu3jo.elephant.chat.dto.PrivateChatMessageResponseDto;
import com.sangbu3jo.elephant.chat.entity.*;
import com.sangbu3jo.elephant.chat.repository.GroupChatRoomRepository;
import com.sangbu3jo.elephant.chat.repository.GroupChatUserRepository;
import com.sangbu3jo.elephant.chat.repository.PrivateChatRoomRepository;
import com.sangbu3jo.elephant.notification.service.NotificationService;
import com.sangbu3jo.elephant.users.entity.User;
import com.sangbu3jo.elephant.users.entity.UserRoleEnum;
import com.sangbu3jo.elephant.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    ChatService chatService;

    @Mock
    UserRepository userRepository;

    @Mock
    PrivateChatRoomRepository privateChatRoomRepository;

    @Mock
    GroupChatRoomRepository groupChatRoomRepository;

    @Mock
    GroupChatUserRepository groupChatUserRepository;

    @Mock
    NotificationService notificationService;

    @Mock
    MongoTemplate mongoTemplate;

    @Mock
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void init() {
        this.chatService = new ChatService(
                userRepository, privateChatRoomRepository, groupChatRoomRepository,
                groupChatUserRepository, notificationService, mongoTemplate
        );
    }

    @Test
    @DisplayName("개인 채팅방 URL 반환 (1:1 새로 생성)")
    void findFirstPrivateChatRoom() {
        // given
        User user = createUser("su@naver.com", "password", "susu","Hi?");
        User user2 = createUser("ye@gmail.com", "password", "yeye", "hello");
        // List<String> userList = new ArrayList<>(Collections.singletonList(user2.getUsername()));
        List<String> userList = new ArrayList<>();
        userList.add(user2.getUsername());

        // when
        when(privateChatRoomRepository.findByUser1AndUser2(any(String.class), any(String.class))).thenReturn(Optional.ofNullable(null));
        String result = chatService.findPrivateChatRoom(user, userList);

        // then
        verify(privateChatRoomRepository, times(1)).save(any(PrivateChatRoom.class));
        assert result.contains("/api/chatRooms/");
    }

    @Test
    @DisplayName("개인 채팅방 URL 반환 (1:1 기존에 존재)")
    void findPrivateChatRoom() {
        // given
        User user = createUser("su@naver.com", "password", "susu","Hi?");
        User user2 = createUser("ye@gmail.com", "password", "yeye", "hello");
        List<String> userList = new ArrayList<>();
        userList.add(user2.getUsername());
        PrivateChatRoom privateChatRoom = new PrivateChatRoom(user.getUsername(), user2.getUsername());

        // when
        when(privateChatRoomRepository.findByUser1AndUser2(any(String.class), any(String.class))).thenReturn(Optional.of(privateChatRoom));
        String result = chatService.findPrivateChatRoom(user, userList);

        // then
        verify(privateChatRoomRepository, times(0)).save(any(PrivateChatRoom.class));
        assert result.equals("/api/chatRooms/" + privateChatRoom.getTitle());
    }

    @Test
    @DisplayName("단체 채팅방 URL 반환 (단체 채팅방 생성)")
    void findPrivateGroupChatRoom() {
        // given
        User user = createUser("su@naver.com", "password", "susu","Hi?");
        User user2 = createUser("ye@gmail.com", "password", "yeye", "hello");
        User user3 = createUser("on@gmail.com", "password", "onon", "HEHE");
        List<String> userList = new ArrayList<>();
        userList.add(user2.getUsername());
        userList.add(user3.getUsername());

        // when
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(userRepository.findByUsername(user2.getUsername())).thenReturn(Optional.of(user2));
        when(userRepository.findByUsername(user3.getUsername())).thenReturn(Optional.of(user3));

        String result = chatService.findPrivateChatRoom(user, userList);

        // then
        verify(groupChatRoomRepository, times(1)).save(any(GroupChatRoom.class));
        verify(userRepository, times(userList.size())).findByUsername(any(String.class));
        verify(groupChatUserRepository, times(userList.size())).save(any(GroupChatUser.class));
        assert result.contains("/api/chatRooms/");
    }

    @Test
    @DisplayName("채팅 메세지 저장 성공")
    void savePrivateChatMessage() {
        // given
        User user = createUser("aa@naver.com", "password", "aaaa","Hi?");
        PrivateChatRoom privateChatRoom = new PrivateChatRoom(user.getUsername(), "bb@naver.com");
        var privateChatMessageRequestDto = PrivateChatMessageRequestDto.builder()
                .username(user.getUsername()).nickname(user.getNickname())
                .title(privateChatRoom.getTitle()).type(MessageType.TALK).message("안녕하세요?").build();

        // when
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        PrivateChatMessageResponseDto responseDto = chatService.savePrivateChatMessage(privateChatMessageRequestDto);

        // then
        verify(userRepository, times(1)).findByUsername(user.getUsername());
        verify(mongoTemplate, times(1)).save(any(PrivateChatMessage.class), eq(privateChatMessageRequestDto.getTitle()));
        assert responseDto.getUsername().equals(user.getUsername());
    }

    @Test
    @DisplayName("개인 채팅방 이름 생성 성공")
    void findPrivateRoom() {
        // given
        String user1 = "ss@gmail.com";
        String user2 = "yy@naver.com";
        PrivateChatRoom privateChatRoom = new PrivateChatRoom(user1, user2);

        // when
        when(privateChatRoomRepository.findByTitle(any(String.class))).thenReturn(Optional.ofNullable(privateChatRoom));
        Model model = new ExtendedModelMap();
        chatService.findGroupOrPrivate(privateChatRoom.getTitle(), user1, model);

        // then
        verify(privateChatRoomRepository, times(1)).findByTitle(privateChatRoom.getTitle());
        verify(groupChatRoomRepository, times(0)).findByTitle(any(String.class));
        assert model.getAttribute("title").equals(user2);
        assert (Boolean) model.getAttribute("group") == false;
    }

    @Test
    @DisplayName("개인 채팅방 이름 생성 성공2")
    void findPrivateRoom2() {
        // given
        String user1 = "ss@gmail.com";
        String user2 = "yy@naver.com";
        PrivateChatRoom privateChatRoom = new PrivateChatRoom(user1, user2);

        // when
        when(privateChatRoomRepository.findByTitle(any(String.class))).thenReturn(Optional.ofNullable(privateChatRoom));
        Model model = new ExtendedModelMap();
        chatService.findGroupOrPrivate(privateChatRoom.getTitle(), user2, model);

        // then
        verify(privateChatRoomRepository, times(1)).findByTitle(privateChatRoom.getTitle());
        verify(groupChatRoomRepository, times(0)).findByTitle(any(String.class));
        assert model.getAttribute("title").equals(user1);
        assert (Boolean) model.getAttribute("group") == false;
    }


    @Test
    @DisplayName("단체 채팅방 이름 생성 성공")
    void findGroupRoom() {
        // given
        GroupChatRoom groupChatRoom = new GroupChatRoom();
        GroupChatUser groupChatUser1 = createGroupUser("aa@gmail.com", "password", "aa", "Hi?", groupChatRoom);
        GroupChatUser groupChatUser2 = createGroupUser("bb@gmail.com", "password", "bb", "Hello", groupChatRoom);
        GroupChatUser groupChatUser3 = createGroupUser("cc@gmail.com", "password", "cc", "HiHi", groupChatRoom);
        List<GroupChatUser> groupChatUsers = new ArrayList<>() {{
            add(groupChatUser1);
            add(groupChatUser2);
            add(groupChatUser3);
        }};

        // when
        when(privateChatRoomRepository.findByTitle(groupChatRoom.getTitle())).thenReturn(Optional.ofNullable(null));
        when(groupChatRoomRepository.findByTitle(groupChatRoom.getTitle())).thenReturn(groupChatRoom);
        when(groupChatUserRepository.findByGroupChatRoom(groupChatRoom)).thenReturn(groupChatUsers);
        Model model = new ExtendedModelMap();
        chatService.findGroupOrPrivate(groupChatRoom.getTitle(), "aa@gmail.com", model);

        // then
        verify(privateChatRoomRepository, times(1)).findByTitle(any(String.class));
        verify(groupChatRoomRepository, times(1)).findByTitle(any(String.class));
        assert !model.getAttribute("title").toString().contains("aa@gmail.com");
        assert (Boolean) model.getAttribute("group") == true;
    }

    @Test
    @DisplayName("개인 채팅방 (단체) 떠나기 성공 (아직 사람들 존재)")
    void leaveGroupChatRoom() {
        // given
        GroupChatRoom groupChatRoom = new GroupChatRoom();
        User user = createUser("su@naver.com", "password", "susu","Hi?");
        User user2 = createUser("ye@gmail.com", "password", "yeye", "hello");
        GroupChatUser groupChatUser = new GroupChatUser(user, LocalDateTime.now(), groupChatRoom);
        GroupChatUser groupChatUser2 = new GroupChatUser(user2, LocalDateTime.now(), groupChatRoom);
        groupChatRoom.addGroupChatUser(groupChatUser);
        groupChatRoom.addGroupChatUser(groupChatUser2);

        // when
        when(groupChatRoomRepository.findByTitle(groupChatRoom.getTitle())).thenReturn(groupChatRoom);
        when(groupChatUserRepository.findByUserAndGroupChatRoom(user, groupChatRoom)).thenReturn(Optional.of(groupChatUser));
        chatService.leavePrivateChatRoom(user, groupChatRoom.getTitle());

        // then
        verify(groupChatUserRepository, times(1)).delete(groupChatUser);
        verify(groupChatRoomRepository, times(0)).delete(groupChatRoom);
    }

    @Test
    @DisplayName("개인 채팅방 (단체) 떠나기 성공 (채팅방도 삭제)")
    void leaveGroupChatRoomAndDeleteGroupChatRoom() {
        // given
        GroupChatRoom groupChatRoom = new GroupChatRoom();
        User user = createUser("su@naver.com", "password", "susu","Hi?");
        GroupChatUser groupChatUser = new GroupChatUser(user, LocalDateTime.now(), groupChatRoom);
        groupChatRoom.addGroupChatUser(groupChatUser);

        // when
        when(groupChatRoomRepository.findByTitle(groupChatRoom.getTitle())).thenReturn(groupChatRoom);
        when(groupChatUserRepository.findByUserAndGroupChatRoom(user, groupChatRoom)).thenReturn(Optional.of(groupChatUser));
        chatService.leavePrivateChatRoom(user, groupChatRoom.getTitle());

        // then
        verify(groupChatUserRepository, times(1)).delete(groupChatUser);
        verify(groupChatRoomRepository, times(1)).delete(groupChatRoom);
    }

    public User createUser(String username, String password, String nickname, String introduction) {
        var signupRequestDto = SignupRequestDto.builder()
                .username(username)
                .password(password)
                .nickname(nickname)
                .introduction(introduction).build();
        String encodepassword = passwordEncoder.encode(signupRequestDto.getPassword());
        return new User(signupRequestDto, encodepassword, UserRoleEnum.USER);
    }

    public GroupChatUser createGroupUser(String username, String password, String nickname, String introduction, GroupChatRoom groupChatRoom) {
        User user = createUser(username, password, nickname, introduction);
        GroupChatUser groupChatUser = new GroupChatUser(user, LocalDateTime.now(), groupChatRoom);
        groupChatRoom.addGroupChatUser(groupChatUser);
        return groupChatUser;
    }

}