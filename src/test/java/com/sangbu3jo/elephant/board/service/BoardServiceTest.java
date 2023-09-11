package com.sangbu3jo.elephant.board.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sangbu3jo.elephant.auth.dto.SignupRequestDto;
import com.sangbu3jo.elephant.board.dto.BoardRequestDto;
import com.sangbu3jo.elephant.board.dto.BoardResponseDto;
import com.sangbu3jo.elephant.board.entity.Board;
import com.sangbu3jo.elephant.board.repository.BoardRepository;
import com.sangbu3jo.elephant.boarduser.entity.BoardUser;
import com.sangbu3jo.elephant.boarduser.entity.BoardUserRoleEnum;
import com.sangbu3jo.elephant.boarduser.repository.BoardUserRepository;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @InjectMocks
    BoardService boardService;

    @Mock
    BoardRepository boardRepository;
    @Mock
    BoardUserRepository boardUserRepository;
    @Mock
    ChatRoomRepository chatRoomRepository;
    @Mock
    ChatUserRepository chatUserRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    JPAQueryFactory jpaQueryFactory;
    @Mock
    NotificationService notificationService;
    @Mock
    MongoTemplate mongoTemplate;
    @Mock
    PasswordEncoder passwordEncoder; // private final로 정의되지는 않았으나 사용하려면 Mock 에너테이션 필요

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        boardService = new BoardService(
                this.boardRepository = boardRepository,
                this.boardUserRepository = boardUserRepository,
                this.chatRoomRepository = chatRoomRepository,
                this.chatUserRepository = chatUserRepository,
                this.userRepository = userRepository,
                this.jpaQueryFactory = jpaQueryFactory,
                this.notificationService = notificationService,
                this.mongoTemplate = mongoTemplate
        );
    }

    @Test
    @DisplayName("프로젝트(보드) 생성 성공")
    void createBoard() {
        // given
        var boardRequestDto = BoardRequestDto.builder()
                .title("미니프로젝트")
                .content("한달 동안 미니프로젝트를 진행합니다")
                .expiredAt(LocalDate.ofEpochDay(2023-10-04)).build();
        Board board = makeBoard();
        var signupRequestDto = SignupRequestDto.builder()
                .username("su@naver.com")
                .password("password")
                .nickname("susu")
                .introduction("Hi~").build();
        String password = passwordEncoder.encode(signupRequestDto.getPassword());
        User user = new User(signupRequestDto, password, UserRoleEnum.USER);
        BoardUser boardUser = new BoardUser(board, user, BoardUserRoleEnum.MANAGER);

        // when
        when(boardRepository.save(any(Board.class))).thenReturn(board);
        when(boardUserRepository.save(any(BoardUser.class))).thenReturn(boardUser);
        BoardResponseDto boardResponseDto = boardService.createBoard(user, boardRequestDto);

        // then
        assert boardResponseDto.getTitle().equals(board.getTitle());
        assert boardResponseDto.getContent().equals(board.getContent());
        assert boardResponseDto.getExpiredAt().equals(board.getExpiredAt());
        then(boardRepository).should(times(1)).save(any());
        then(boardUserRepository).should(times(1)).save(any());
    }

    @Test
    @DisplayName("프로젝트(보드) 수정 성공")
    void updateBoard() {
        // given
        Board board = makeBoard();
        User user = makeUser();
        BoardUser boardUser = new BoardUser(board, user, BoardUserRoleEnum.MANAGER);

        var updateboardRequestDto = BoardRequestDto.builder()
                .title("미니프로젝트")
                .content("두 달 동안 미니프로젝트를 진행합니다")
                .expiredAt(LocalDate.ofEpochDay(2023-11-04)).build();

        // when
        /* 해당 findById가 Optional로 감싸져 반환되기 때문에 Optional로 감싸주어야 함
        * 이미 위에서 board와 boarduser를 만들었기 때문에 Optional.of로 감쌈 */
        when(boardRepository.findById(any(Long.class))).thenReturn(Optional.of(board));
        when(boardUserRepository.findByBoardAndUser(any(Board.class), any(User.class))).thenReturn(Optional.of(boardUser));
        BoardResponseDto boardResponseDto = boardService.updateBoard(1L, user, updateboardRequestDto);

        // then
        assert boardResponseDto.getTitle().equals(updateboardRequestDto.getTitle());
        assert boardResponseDto.getContent().equals(updateboardRequestDto.getContent());
        assert boardResponseDto.getExpiredAt().equals(updateboardRequestDto.getExpiredAt());
    }

    @Test
    @DisplayName("프로젝트(보드) 삭제 (채팅방 없음) 성공")
    void deleteBoard() {
        // given
        Board board = makeBoard();
        User user = makeUser();
        BoardUser boardUser = new BoardUser(board, user, BoardUserRoleEnum.MANAGER);

        // when
        when(boardRepository.findById(any(Long.class))).thenReturn(Optional.of(board));
        when(boardUserRepository.findByBoardAndUser(any(Board.class), any(User.class))).thenReturn(Optional.of(boardUser));
        when(mongoTemplate.getCollection(any(String.class))).thenReturn(null); // 컬렉션이 존재하지 않는 경우(채팅내역 X)
        boardService.deleteBoard(1L, user);

        // then
        verify(boardRepository, times(1)).delete(any(Board.class));
        verify(mongoTemplate, times(1)).getCollection(anyString());
    }

    @Test
    @DisplayName("프로젝트(보드) 삭제 (채팅방 있음) 성공")
    void deleteBoardWithChat() {
        // given
        Board board = makeBoard();
        User user = makeUser();
        BoardUser boardUser = new BoardUser(board, user, BoardUserRoleEnum.MANAGER);
        ChatRoom chatRoom = new ChatRoom(1L, board);
        ChatUser chatUser = new ChatUser(user.getUsername(), LocalDateTime.now());
        chatUser.updateChatRoom(chatRoom);
        board.updateChatRoom(chatRoom);

        // when
        when(boardRepository.findById(any(Long.class))).thenReturn(Optional.of(board));
        when(boardUserRepository.findByBoardAndUser(any(Board.class), any(User.class))).thenReturn(Optional.of(boardUser));
        when(mongoTemplate.getCollection(any(String.class))).thenReturn(null); // 컬렉션이 존재하지 않는 경우(채팅내역 X)
//        when(chatRoomRepository.findById(any(Long.class))).thenReturn(Optional.of(chatRoom));
//        when(chatUserRepository.findAllByChatroom(any(ChatRoom.class))).thenReturn(List.of(chatUser));

        boardService.deleteBoard(1L, user);

        // then
//        verify(chatUserRepository, times(1)).deleteAll(List.of(chatUser));
//        verify(chatRoomRepository, times(1)).delete(any(ChatRoom.class));
        verify(boardRepository, times(1)).delete(any(Board.class));
        verify(mongoTemplate, times(1)).getCollection(anyString());
    }
    
    @Test
    @DisplayName("프로젝트(보드)에 유저 초대(로그인 X) 성공")
    void inviteUser() {
        // given
        User user = makeUser();
        Board board = makeBoard();
        BoardUser boardUser = new BoardUser(board, user, BoardUserRoleEnum.MANAGER);

        // when
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(user));
        when(boardRepository.findById(any(Long.class))).thenReturn(Optional.of(board));
        when(boardUserRepository.findByBoardAndUser(any(Board.class), any(User.class))).thenReturn(Optional.ofNullable(null));
        when(boardUserRepository.save(any(BoardUser.class))).thenReturn(boardUser);
        when(boardUserRepository.findAllByBoardId(any(Long.class))).thenReturn(List.of());
        String result = boardService.inviteUser(null, 1L, user.getUsername());

        // then
        assert result.equals("login-page");
    }

    @Test
    @DisplayName("프로젝트(보드) 떠나기 - 매니저")
    void leaveBoard() {
        // given
        User user = makeUser();
        Board board = makeBoard();
        BoardUser boardUser = new BoardUser(board, user, BoardUserRoleEnum.MANAGER);

        // when
        when(boardRepository.findById(any(Long.class))).thenReturn(Optional.of(board));
        when(boardUserRepository.findByBoardAndUser(board, user)).thenReturn(Optional.of(boardUser));
        ResponseEntity<String> response = boardService.leaveBoard(user, 1L);

        // then
        assert response.getStatusCode().value() == 400;
    }
    
    @Test
    @DisplayName("프로젝트(보드) 떠나기 - 멤버")
    void leaveBoardUser() {
        // given
        User user = makeUser();
        Board board = makeBoard();
        BoardUser boardUser = new BoardUser(board, user, BoardUserRoleEnum.MEMBER);

        // when
        when(boardRepository.findById(any(Long.class))).thenReturn(Optional.of(board));
        when(boardUserRepository.findByBoardAndUser(board, user)).thenReturn(Optional.of(boardUser));
        ResponseEntity<String> response = boardService.leaveBoard(user, 1L);

        // then
        assert response.getStatusCode().value() == 200;
    }

    public User makeUser() {
        var signupRequestDto = SignupRequestDto.builder()
                .username("su@naver.com")
                .password("password")
                .nickname("susu")
                .introduction("Hi~").build();
        String password = passwordEncoder.encode(signupRequestDto.getPassword());
        User user = new User(signupRequestDto, password, UserRoleEnum.USER);
        return user;
    }

    public Board makeBoard() {
        var boardRequestDto = BoardRequestDto.builder()
                .title("미니프로젝트")
                .content("한달 동안 미니프로젝트를 진행합니다")
                .expiredAt(LocalDate.ofEpochDay(2023-10-04)).build();
        Board board = new Board(boardRequestDto);
        return board;
    }





}