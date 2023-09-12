package com.sangbu3jo.elephant.chat.service;

import com.sangbu3jo.elephant.auth.dto.SignupRequestDto;
import com.sangbu3jo.elephant.board.dto.BoardRequestDto;
import com.sangbu3jo.elephant.board.entity.Board;
import com.sangbu3jo.elephant.board.repository.BoardRepository;
import com.sangbu3jo.elephant.boarduser.entity.BoardUser;
import com.sangbu3jo.elephant.boarduser.entity.BoardUserRoleEnum;
import com.sangbu3jo.elephant.chat.dto.ChatMessageRequestDto;
import com.sangbu3jo.elephant.chat.dto.MessageType;
import com.sangbu3jo.elephant.chat.dto.PrivateChatMessageRequestDto;
import com.sangbu3jo.elephant.chat.entity.*;
import com.sangbu3jo.elephant.chat.repository.*;
import com.sangbu3jo.elephant.notification.service.NotificationService;
import com.sangbu3jo.elephant.users.entity.User;
import com.sangbu3jo.elephant.users.entity.UserRoleEnum;
import com.sangbu3jo.elephant.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @InjectMocks
    ChatRoomService chatRoomService;

    @Mock
    UserRepository userRepository;
    @Mock
    ChatRoomRepository chatRoomRepository;
    @Mock
    ChatUserRepository chatUserRepository;
    @Mock
    BoardRepository boardRepository;
    @Mock
    MongoTemplate mongoTemplate;
    @Mock
    PrivateChatRoomRepository privateChatRoomRepository;
    @Mock
    GroupChatRoomRepository groupChatRoomRepository;
    @Mock
    GroupChatUserRepository groupChatUserRepository;
    @Mock
    NotificationService notificationService;
    @Mock
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void init() {
        this.chatRoomService = new ChatRoomService(
                userRepository, chatRoomRepository, chatUserRepository, boardRepository,
                privateChatRoomRepository, groupChatRoomRepository, groupChatUserRepository, notificationService, mongoTemplate
        );
    }

    @Test
    @DisplayName("단체 채팅방 정보 찾기 성공")
    void findChatRoom() {
//        // given
//        Board board = board();
//
//        // when
//        when(chatRoomRepository.findById(any(Long.class))).thenReturn(Optional.ofNullable(null));
//        chatRoomService.findChatRoom(board);
//
//        // then
//        verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));
//        verify(boardRepository, times(1)).save(any(Board.class));
    }

    @Test
    @DisplayName("단체 채팅 메세지 저장 성공")
    void saveChatMessage() {
        // given
        User user = user("su@naver.com", "password", "susu","Hi?");
        var chatMessageRequestDto = ChatMessageRequestDto.builder()
                .chatRoomId(1L).username(user.getUsername()).nickname(user.getNickname()).type(MessageType.TALK).build();
        chatMessageRequestDto.setMessage("안녕하세요?");

        // when
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(user));
        chatRoomService.saveChatMessage(chatMessageRequestDto);

        // then
        verify(mongoTemplate, times(1)).save(any(ChatMessage.class), eq(chatMessageRequestDto.getChatRoomId().toString()));
    }

    @Test
    @DisplayName("단체 채팅방 참여 여부 확인(처음 들어옴) 성공")
    void findUsersInChatRoom() {
//        // given
//        Board board = board();
//        ChatRoom chatRoom = chatRoom(board);
//        User user = user("su@naver.com", "password", "susu","Hi?");
//
//        // when
//        when(chatRoomRepository.findById(any(Long.class))).thenReturn(Optional.of(chatRoom));
//        when(chatUserRepository.findByUsernameAndChatroom(any(String.class), any(ChatRoom.class))).thenReturn(Optional.ofNullable(null));
//        Boolean users = chatRoomService.findUsersInChatRoom(user.getUsername(), 1L, LocalDateTime.now());
//
//        // then
//        assert users == true;
    }

    @Test
    @DisplayName("단체 채팅방 참여 여부 확인(들어온 적 있음) 성공")
    void findUsersInChatRoomBefore() {
//        // given
//        Board board = board();
//        ChatRoom chatRoom = chatRoom(board);
//        User user = user("su@naver.com", "password", "susu","Hi?");
//        ChatUser chatUser = new ChatUser(user.getUsername(), LocalDateTime.now());
//        chatUser.updateChatRoom(chatRoom);
//
//        // when
//        when(chatRoomRepository.findById(any(Long.class))).thenReturn(Optional.of(chatRoom));
//        when(chatUserRepository.findByUsernameAndChatroom(any(String.class), any(ChatRoom.class))).thenReturn(Optional.of(chatUser));
//        Boolean users = chatRoomService.findUsersInChatRoom(user.getUsername(), 1L, LocalDateTime.now());
//
//        // then
//        assert users == false;
    }

    @Test
    @DisplayName("개인 채팅방 URL 반환 (1:1 새로 생성)")
    void findFirstPrivateChatRoom() {
        // given
        User user = user("su@naver.com", "password", "susu","Hi?");
        User user2 = user("ye@gmail.com", "password", "yeye", "hello");
        ArrayList arrayList = new ArrayList<>();
        arrayList.add(user2.getUsername());

        // when
        when(privateChatRoomRepository.findByUser1AndUser2(any(String.class), any(String.class))).thenReturn(Optional.ofNullable(null));
        String result = chatRoomService.findPrivateChatRoom(user, arrayList);

        // then
        verify(privateChatRoomRepository, times(1)).save(any(PrivateChatRoom.class));
        assert result.contains("/api/chatRooms/");
    }

    @Test
    @DisplayName("개인 채팅방 URL 반환 (1:1 기존에 존재)")
    void findPrivateChatRoom() {
        // given
        User user = user("su@naver.com", "password", "susu","Hi?");
        User user2 = user("ye@gmail.com", "password", "yeye", "hello");
        ArrayList arrayList = new ArrayList<>();
        arrayList.add(user2.getUsername());
        PrivateChatRoom privateChatRoom = new PrivateChatRoom(user.getUsername(), user2.getUsername());

        // when
        when(privateChatRoomRepository.findByUser1AndUser2(any(String.class), any(String.class))).thenReturn(Optional.of(privateChatRoom));
        String result = chatRoomService.findPrivateChatRoom(user, arrayList);

        // then
        verify(privateChatRoomRepository, times(0)).save(any(PrivateChatRoom.class));
        assert result.equals("/api/chatRooms/" + privateChatRoom.getTitle());
    }

    @Test
    @DisplayName("단체 채팅방 URL 반환 (단체 채팅방 생성)")
    void findPrivateGroupChatRoom() {
        // given
        User user = user("su@naver.com", "password", "susu","Hi?");
        User user2 = user("ye@gmail.com", "password", "yeye", "hello");
        User user3 = user("on@gmail.com", "password", "onon", "HEHE");
        ArrayList arrayList = new ArrayList<>();
        arrayList.add(user2.getUsername());
        arrayList.add(user3.getUsername());

        // when
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(userRepository.findByUsername(user2.getUsername())).thenReturn(Optional.of(user2));
        when(userRepository.findByUsername(user3.getUsername())).thenReturn(Optional.of(user3));

        String result = chatRoomService.findPrivateChatRoom(user, arrayList);

        // then
        verify(groupChatRoomRepository, times(1)).save(any(GroupChatRoom.class));
        verify(userRepository, times(arrayList.size())).findByUsername(any(String.class));
        verify(groupChatUserRepository, times(arrayList.size())).save(any(GroupChatUser.class));
        assert result.contains("/api/chatRooms/");
    }

    @Test
    @DisplayName("개인 채팅 메세지 저장 성공")
    void savePrivateChatMessage() {
        // given
        User user = user("aa@naver.com", "password", "aaaa","Hi?");
        PrivateChatRoom privateChatRoom = new PrivateChatRoom(user.getUsername(), "bb@naver.com");
        var privateChatMessageRequestDto = PrivateChatMessageRequestDto.builder()
                .username(user.getUsername()).nickname(user.getNickname())
                .title(privateChatRoom.getTitle()).type(MessageType.TALK).message("안녕하세요?").build();

        // when
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        chatRoomService.savePrivateChatMessage(privateChatMessageRequestDto);

        // then
        verify(userRepository, times(1)).findByUsername(user.getUsername());
        verify(mongoTemplate, times(1)).save(any(PrivateChatMessage.class), eq(privateChatMessageRequestDto.getTitle()));
    }

    @Test
    @DisplayName("개인 채팅방 (1:1) 판별 성공")
    void findOutGroupChatRoom() {
        // given
        PrivateChatRoom privateChatRoom = new PrivateChatRoom("aa@gmail.com", "bb@naver.com");

        // when
        when(privateChatRoomRepository.findByTitle(privateChatRoom.getTitle())).thenReturn(Optional.of(privateChatRoom));
        String findOut = chatRoomService.findGroupOrPrivate(privateChatRoom.getTitle(), "aa@gmail.com");

        // then
        assert findOut == "bb@naver.com";
    }


    @Test
    @DisplayName("개인 채팅방 (단체) 판별 성공")
    void findOutPrivateChatRoom() {
//        // given
//        GroupChatRoom groupChatRoom = new GroupChatRoom();
//
//        // when
//        when(privateChatRoomRepository.findByTitle(groupChatRoom.getTitle())).thenReturn(Optional.ofNullable(null));
//        Boolean findOut = chatRoomService.findGroupOrPrivate(groupChatRoom.getTitle());
//
//        // then
//        assert findOut == true;
    }

    @Test
    @DisplayName("개인 채팅방 (단체) 떠나기 성공 (아직 사람들 존재)")
    void leaveGroupChatRoom() {
        // given
        GroupChatRoom groupChatRoom = new GroupChatRoom();
        User user = user("su@naver.com", "password", "susu","Hi?");
        User user2 = user("ye@gmail.com", "password", "yeye", "hello");
        GroupChatUser groupChatUser = new GroupChatUser(user, LocalDateTime.now(), groupChatRoom);
        GroupChatUser groupChatUser2 = new GroupChatUser(user2, LocalDateTime.now(), groupChatRoom);
        groupChatRoom.addGroupChatUser(groupChatUser);
        groupChatRoom.addGroupChatUser(groupChatUser2);

        // when
        when(groupChatRoomRepository.findByTitle(groupChatRoom.getTitle())).thenReturn(groupChatRoom);
        when(groupChatUserRepository.findByUserAndGroupChatRoom(user, groupChatRoom)).thenReturn(Optional.of(groupChatUser));
        chatRoomService.leavePrivateChatRoom(user, groupChatRoom.getTitle());

        // then
        verify(groupChatUserRepository, times(1)).delete(groupChatUser);
        verify(groupChatRoomRepository, times(0)).delete(groupChatRoom);
    }

    @Test
    @DisplayName("개인 채팅방 (단체) 떠나기 성공 (채팅방도 삭제)")
    void leaveGroupChatRoomAndDeleteGroupChatRoom() {
        // given
        GroupChatRoom groupChatRoom = new GroupChatRoom();
        User user = user("su@naver.com", "password", "susu","Hi?");
        GroupChatUser groupChatUser = new GroupChatUser(user, LocalDateTime.now(), groupChatRoom);
        groupChatRoom.addGroupChatUser(groupChatUser);

        // when
        when(groupChatRoomRepository.findByTitle(groupChatRoom.getTitle())).thenReturn(groupChatRoom);
        when(groupChatUserRepository.findByUserAndGroupChatRoom(user, groupChatRoom)).thenReturn(Optional.of(groupChatUser));
        chatRoomService.leavePrivateChatRoom(user, groupChatRoom.getTitle());

        // then
        verify(groupChatUserRepository, times(1)).delete(groupChatUser);
        verify(groupChatRoomRepository, times(1)).delete(groupChatRoom);
    }


    public Board board () {
        var boardRequestDto = BoardRequestDto.builder()
                .title("미니프로젝트")
                .content("한달 동안 미니프로젝트를 진행합니다")
                .expiredAt(LocalDate.ofEpochDay(2023-10-04)).build();
        Board board = new Board(boardRequestDto);
        board.updateId(1L);
        var signupRequestDto = SignupRequestDto.builder()
                .username("su@naver.com")
                .password("password")
                .nickname("susu")
                .introduction("Hi~").build();
        String password = passwordEncoder.encode(signupRequestDto.getPassword());
        User user = new User(signupRequestDto, password, UserRoleEnum.USER);
        BoardUser boardUser = new BoardUser(board, user, BoardUserRoleEnum.MANAGER);
        return board;
    }

    public User user(String username, String password, String nickname, String introduction) {
        var signupRequestDto = SignupRequestDto.builder()
                .username(username)
                .password(password)
                .nickname(nickname)
                .introduction(introduction).build();
        String encodepassword = passwordEncoder.encode(signupRequestDto.getPassword());
        return new User(signupRequestDto, encodepassword, UserRoleEnum.USER);
    }

    public ChatRoom chatRoom(Board board) {
        return new ChatRoom(board.getId(), board);
    }
}