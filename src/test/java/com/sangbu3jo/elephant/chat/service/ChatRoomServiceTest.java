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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import com.sangbu3jo.elephant.chat.repository.ChatRoomRepository;
import com.sangbu3jo.elephant.chat.repository.ChatUserRepository;
import com.sangbu3jo.elephant.users.entity.User;
import com.sangbu3jo.elephant.users.entity.UserRoleEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @InjectMocks
    ChatRoomService chatRoomService;

    @Mock
    ChatRoomRepository chatRoomRepository;
    @Mock
    ChatUserRepository chatUserRepository;
    @Mock
    BoardRepository boardRepository;
    @Mock
    MongoTemplate mongoTemplate;
    @Mock
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void init() {
        this.chatRoomService = new ChatRoomService(
                this.chatRoomRepository = chatRoomRepository,
                this.chatUserRepository = chatUserRepository,
                this.boardRepository = boardRepository,
                this.mongoTemplate = mongoTemplate
        );
    }

    @Test
    @DisplayName("단체 채팅방 정보 찾기 성공")
    void findChatRoom() {
        // given
        Board board = board();

        // when
        when(chatRoomRepository.findById(any(Long.class))).thenReturn(Optional.ofNullable(null));
        chatRoomService.findChatRoom(board);

        // then
        verify(chatRoomRepository, times(1)).save(any(ChatRoom.class));
        verify(boardRepository, times(1)).save(any(Board.class));
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
        chatRoomService.saveChatMessage(chatMessageRequestDto);

        // then
        verify(mongoTemplate, times(1)).save(any(ChatMessage.class), eq(chatMessageRequestDto.getChatRoomId().toString()));
    }

    @Test
    @DisplayName("단체 채팅방 참여 여부 확인(처음 들어옴) 성공")
    void findUsersInChatRoom() {
        // given
        Board board = board();
        ChatRoom chatRoom = chatRoom(board);
        User user = user("su@naver.com", "password", "susu","Hi?");

        // when
        when(chatRoomRepository.findById(any(Long.class))).thenReturn(Optional.of(chatRoom));
        when(chatUserRepository.findByUsernameAndChatroom(any(String.class), any(ChatRoom.class))).thenReturn(Optional.ofNullable(null));
        Boolean users = chatRoomService.findUsersInChatRoom(user.getUsername(), 1L, LocalDateTime.now());

        // then
        assert users == true;
    }

    @Test
    @DisplayName("단체 채팅방 참여 여부 확인(들어온 적 있음) 성공")
    void findUsersInChatRoomBefore() {
        // given
        Board board = board();
        ChatRoom chatRoom = chatRoom(board);
        User user = user("su@naver.com", "password", "susu","Hi?");
        ChatUser chatUser = new ChatUser(user.getUsername(), LocalDateTime.now());
        chatUser.updateChatRoom(chatRoom);

        // when
        when(chatRoomRepository.findById(any(Long.class))).thenReturn(Optional.of(chatRoom));
        when(chatUserRepository.findByUsernameAndChatroom(any(String.class), any(ChatRoom.class))).thenReturn(Optional.of(chatUser));
        Boolean users = chatRoomService.findUsersInChatRoom(user.getUsername(), 1L, LocalDateTime.now());

        // then
        assert users == false;
    }

    @Test
    @DisplayName("개인 채팅방 URL 반환 (새로 생성)")
    void findFirstPrivateChatRoom() {
        // given
        String firstUser = "aa@naver.com";
        String secondUser = "bb@gmail.com";
        PrivateChatRoom chatRoom = new PrivateChatRoom(firstUser, secondUser);

        // when
        when(mongoTemplate.findOne(any(Query.class), eq(PrivateChatRoom.class))).thenReturn(null);
        when(mongoTemplate.save(any(PrivateChatRoom.class))).thenReturn(chatRoom);
        chatRoomService.findPrivateChatRoom(firstUser, secondUser);

        // then
        verify(mongoTemplate, times(1)).save(any(PrivateChatRoom.class));
    }

    @Test
    @DisplayName("개인 채팅방 URL 반환 (기존에 존재)")
    void findPrivateChatRoom() {
        // given
        String firstUser = "aa@naver.com";
        String secondUser = "bb@gmail.com";
        PrivateChatRoom chatRoom = new PrivateChatRoom(firstUser, secondUser);

        // when
        when(mongoTemplate.findOne(any(Query.class), eq(PrivateChatRoom.class))).thenReturn(chatRoom);
        String url = chatRoomService.findPrivateChatRoom(firstUser, secondUser);

        // then
        assert url.equals("/api/chatRooms/" + chatRoom.getTitle());
    }

    @Test
    @DisplayName("개인 채팅 메세지 저장 성공")
    void savePrivateChatMessage() {
        // given
        String firstUser = "aa@naver.com";
        User user = user("aa@naver.com", "password", "aaaa","Hi?");

        String secondUser = "bb@gmail.com";
        PrivateChatRoom chatRoom = new PrivateChatRoom(firstUser, secondUser);
        var chatMessageRequestDto = PrivateChatMessageRequestDto.builder()
                .title(chatRoom.getTitle()).username(user.getUsername()).nickname(user.getNickname()).type(MessageType.TALK).build();
        chatMessageRequestDto.setMessage("안녕하세요?");

        // when
        chatRoomService.savePrivateChatMessage(chatMessageRequestDto);

        // then
        verify(mongoTemplate, times(1)).save(any(PrivateChatMessage.class), eq(chatRoom.getTitle().toString()));
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