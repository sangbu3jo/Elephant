package com.sangbu3jo.elephant.chat.service;

import com.sangbu3jo.elephant.auth.dto.SignupRequestDto;
import com.sangbu3jo.elephant.board.dto.BoardRequestDto;
import com.sangbu3jo.elephant.board.entity.Board;
import com.sangbu3jo.elephant.board.repository.BoardRepository;
import com.sangbu3jo.elephant.chat.dto.ChatMessageRequestDto;
import com.sangbu3jo.elephant.chat.dto.ChatMessageResponseDto;
import com.sangbu3jo.elephant.chat.dto.MessageType;
import com.sangbu3jo.elephant.chat.entity.ChatMessage;
import com.sangbu3jo.elephant.chat.entity.ChatRoom;
import com.sangbu3jo.elephant.chat.entity.ChatUser;
import com.sangbu3jo.elephant.chat.repository.ChatRoomRepository;
import com.sangbu3jo.elephant.chat.repository.ChatUserRepository;
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

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectChatServiceTest {

    ProjectChatService projectChatService;

    @Mock
    UserRepository userRepository;

    @Mock
    ChatRoomRepository chatRoomRepository;

    @Mock
    ChatUserRepository chatUserRepository;

    @Mock
    BoardRepository boardRepository;

    @Mock
    NotificationService notificationService;

    @Mock
    MongoTemplate mongoTemplate;

    @Mock
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void init() {
        this.projectChatService = new ProjectChatService(
                userRepository, chatRoomRepository, chatUserRepository,
                boardRepository, notificationService, mongoTemplate
        );
    }

    @Test
    @DisplayName("단체 채팅방 정보 생성 성공")
    void findChatRoomSuccess() {
        // given
        Board board = createBoard();

        // when
        when(chatRoomRepository.findByRoomId(1L)).thenReturn(Optional.ofNullable(null));
        String title = projectChatService.findChatRoom(board);

        // then
        verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));
        verify(boardRepository, times(1)).save(any(Board.class));
        assert title.equals(board.getTitle());
    }

    @Test
    @DisplayName("단체 채팅방 정보 찾기 성공")
    void findChatRoomSuccess2() {
        // given
        Board board = createBoard();
        ChatRoom chatRoom = createChatRoom(board);

        // when
        when(chatRoomRepository.findByRoomId(1L)).thenReturn(Optional.of(chatRoom));
        String title = projectChatService.findChatRoom(board);

        // then
        verify(chatRoomRepository, times(0)).save(any(ChatRoom.class));
        verify(boardRepository, times(0)).save(any(Board.class));
        assert title.equals(board.getTitle());
    }

    @Test
    @DisplayName("단체 채팅방 메세지 저장 성공")
    void saveChatMessage() {
        // given
        User user = createUser("susu@gmail.com", "password", "susu", "hi");
        ChatMessageRequestDto chatMessageRequestDto = createChatMessage(user);

        // when
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.ofNullable(user));
        when(chatUserRepository.findByChatroom_RoomId(any(Long.class))).thenReturn(Collections.emptyList());
        ChatMessageResponseDto chatMessageResponseDto = projectChatService.saveChatMessage(chatMessageRequestDto);

        // then
        assert chatMessageResponseDto.getMessage().equals(chatMessageRequestDto.getMessage());
        assert chatMessageResponseDto.getUsername().equals(chatMessageResponseDto.getUsername());
    }

    @Test
    @DisplayName("단체 채팅방 참여 여부 확인[false] 성공")
    void findUserInChatRoomSuccess() {
        // given
        Board board = createBoard();
        ChatRoom chatRoom = createChatRoom(board);
        ChatUser chatUser = createChatUser("martina@naver.com");

        // when
        when(chatRoomRepository.findByRoomId(1L)).thenReturn(Optional.of(chatRoom));
        when(chatUserRepository.findByUsernameAndChatroom(any(String.class), any(ChatRoom.class))).thenReturn(Optional.of(chatUser));
        Boolean flag = projectChatService.findUsersInChatRoom(chatUser.getUsername(), board.getId() ,LocalDateTime.now());

        // then
        assert flag == false;
    }

    @Test
    @DisplayName("단체 채팅방 참여 여부 확인[true] 성공")
    void findUserInChatRoomSuccess2() {
        // given
        Board board = createBoard();
        ChatRoom chatRoom = createChatRoom(board);

        // when
        when(chatRoomRepository.findByRoomId(1L)).thenReturn(Optional.of(chatRoom));
        when(chatUserRepository.findByUsernameAndChatroom(any(String.class), any(ChatRoom.class))).thenReturn(Optional.ofNullable(null));
        Boolean flag = projectChatService.findUsersInChatRoom("susu@gmail.com", board.getId(), LocalDateTime.now());

        // then
        verify(chatUserRepository, times(1)).save(any(ChatUser.class));
        assert flag == true;
    }

    private Board createBoard() {
        var boardRequestDto = BoardRequestDto.builder().
                title("최종프로젝트").content("최종프로젝트를 진행합니다.").
                expiredAt(LocalDate.ofEpochDay(2023-10-04)).build();
        var board = Board.builder().boardRequestDto(boardRequestDto).build();
        board.updateId(1L);
        return board;
    }

    private ChatRoom createChatRoom(Board board) {
        ChatRoom chatRoom = new ChatRoom(board.getId(), board);
        board.updateChatRoom(chatRoom);
        return chatRoom;
    }

    private ChatUser createChatUser(String username) {
        return new ChatUser(username, LocalDateTime.now());
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

    public ChatMessageRequestDto createChatMessage(User user) {
        ChatMessageRequestDto chatMessageRequestDto =
                ChatMessageRequestDto.builder()
                .username(user.getUsername()).nickname(user.getNickname()).type(MessageType.TALK)
                .chatRoomId(1L).build();
        chatMessageRequestDto.setMessage("안녕?");
        return chatMessageRequestDto;
    }

    /* 컬렉션 구분이 없으면
    List<ChatMessage> chatMessages = mongoTemplate.find(
                    Query.query(Criteria.where("chatRoomId").is(chatRoomId)),
                    ChatMessage.class
            );*/

}