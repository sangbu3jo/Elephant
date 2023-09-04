package com.sangbu3jo.elephant.card.service;

import com.sangbu3jo.elephant.auth.dto.SignupRequestDto;
import com.sangbu3jo.elephant.board.dto.BoardRequestDto;
import com.sangbu3jo.elephant.board.entity.Board;
import com.sangbu3jo.elephant.board.service.BoardService;
import com.sangbu3jo.elephant.boarduser.dto.BoardUserResponseDto;
import com.sangbu3jo.elephant.boarduser.entity.BoardUser;
import com.sangbu3jo.elephant.boarduser.entity.BoardUserRoleEnum;
import com.sangbu3jo.elephant.boarduser.repository.BoardUserRepository;
import com.sangbu3jo.elephant.card.dto.CardOrderRequestDto;
import com.sangbu3jo.elephant.card.dto.CardRequestDto;
import com.sangbu3jo.elephant.card.dto.CardResponseDto;
import com.sangbu3jo.elephant.card.dto.CardUserRequestDto;
import com.sangbu3jo.elephant.card.entity.Card;
import com.sangbu3jo.elephant.card.repository.CardRepository;
import com.sangbu3jo.elephant.carduser.entity.CardUser;
import com.sangbu3jo.elephant.carduser.repository.CardUserRepository;
import com.sangbu3jo.elephant.columns.dto.ColumnsRequestDto;
import com.sangbu3jo.elephant.columns.entity.Columns;
import com.sangbu3jo.elephant.columns.repository.ColumnsRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @InjectMocks
    CardService cardService;

    @Mock
    BoardService boardService;
    @Mock
    CardRepository cardRepository;
    @Mock
    ColumnsRepository columnsRepository;
    @Mock
    CardUserRepository cardUserRepository;
    @Mock
    BoardUserRepository boardUserRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void init() {
        this.cardService = new CardService(
                this.boardService = boardService,
                this.cardRepository = cardRepository,
                this.columnsRepository = columnsRepository,
                this.cardUserRepository = cardUserRepository,
                this.boardUserRepository = boardUserRepository,
                this.userRepository = userRepository
        );
    }

    @Test
    @DisplayName("카드 생성 성공")
    void createCard() {
        // given
        var boardRequestDto = BoardRequestDto.builder()
                .title("미니프로젝트")
                .content("한달 동안 미니프로젝트를 진행합니다")
                .expiredAt(LocalDate.ofEpochDay(2023-10-04)).build();
        Board board = new Board(boardRequestDto);

        var columnsRequestDto = ColumnsRequestDto.builder()
                .title("To Do").build();
        Columns columns = new Columns(columnsRequestDto, board, 0);

        var cardRequestDto = CardRequestDto.builder()
                .title("와이어프레임").content("전반적인 와이어프레임을 만들어봅시다").expiredAt(LocalDate.ofEpochDay(2023-9-05)).build();

        Card card = new Card(cardRequestDto, columns, 0);

        LocalDate today = LocalDate.now();
        Period period = Period.between(today, LocalDate.ofEpochDay(2023-9-05));
        String Dday = null;
        if (period.isNegative()) {
            Dday = "종료";
        } else if (period.isZero()) {
            Dday = "D-Day";
        } else {
            Dday = "D-" + period.getDays();
        }

        // when
        when(columnsRepository.findById(any(Long.class))).thenReturn(Optional.of(columns));
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        CardResponseDto cardResponseDto = cardService.createCard(1L, cardRequestDto);

        // then
        assert cardResponseDto.getTitle().equals(cardRequestDto.getTitle());
        assert cardResponseDto.getDday().equals(Dday);
    }

    @Test
    @DisplayName("카드 수정 성공")
    void updateCard() {
        // given
        var boardRequestDto = BoardRequestDto.builder()
                .title("미니프로젝트")
                .content("한달 동안 미니프로젝트를 진행합니다")
                .expiredAt(LocalDate.ofEpochDay(2023-10-04)).build();
        Board board = new Board(boardRequestDto);

        var columnsRequestDto = ColumnsRequestDto.builder()
                .title("To Do").build();
        Columns columns = new Columns(columnsRequestDto, board, 0);

        var cardRequestDto = CardRequestDto.builder()
                .title("와이어프레임").content("전반적인 와이어프레임을 만들어봅시다").expiredAt(LocalDate.ofEpochDay(2023-9-05)).build();

        Card card = new Card(cardRequestDto, columns, 0);

        var updateCardRequestDto = CardRequestDto.builder()
                .title("와이어프레임").content("와이어프레임 피그마에서 만들어봅시다").expiredAt(LocalDate.ofEpochDay(2023-9-06)).build();

        LocalDate today = LocalDate.now();
        Period period = Period.between(today, LocalDate.ofEpochDay(2023-9-06));
        String Dday = null;
        if (period.isNegative()) {
            Dday = "종료";
        } else if (period.isZero()) {
            Dday = "D-Day";
        } else {
            Dday = "D-" + period.getDays();
        }

        // when
        when(cardRepository.findById(any(Long.class))).thenReturn(Optional.of(card));
        CardResponseDto cardResponseDto = cardService.updateCard(1L, updateCardRequestDto);

        // then
        assert cardResponseDto.getTitle().equals(updateCardRequestDto.getTitle());
        assert cardResponseDto.getDday().equals(Dday);
    }

    @Test
    @DisplayName("카드 삭제 성공")
    void deleteCard() {
        // given
        var boardRequestDto = BoardRequestDto.builder()
                .title("미니프로젝트")
                .content("한달 동안 미니프로젝트를 진행합니다")
                .expiredAt(LocalDate.ofEpochDay(2023-10-04)).build();
        Board board = new Board(boardRequestDto);

        var columnsRequestDto = ColumnsRequestDto.builder()
                .title("To Do").build();
        Columns columns = new Columns(columnsRequestDto, board, 0);

        var cardRequestDto = CardRequestDto.builder()
                .title("와이어프레임").content("전반적인 와이어프레임을 만들어봅시다").expiredAt(LocalDate.ofEpochDay(2023-9-05)).build();

        Card card = new Card(cardRequestDto, columns, 0);

        columns.addCard(card);

        // when
        when(cardRepository.findById(any(Long.class))).thenReturn(Optional.of(card));
        cardService.deleteCard(1L);

        // then
        verify(cardRepository, times(1)).delete(any(Card.class));
    }

    @Test
    @DisplayName("카드 담당자 변경 성공")
    void updateCardUser() {
        // given
        var boardRequestDto = BoardRequestDto.builder()
                .title("미니프로젝트")
                .content("한달 동안 미니프로젝트를 진행합니다")
                .expiredAt(LocalDate.ofEpochDay(2023-10-04)).build();
        Board board = new Board(boardRequestDto);

        var columnsRequestDto = ColumnsRequestDto.builder()
                .title("To Do").build();
        Columns columns = new Columns(columnsRequestDto, board, 0);

        var cardRequestDto = CardRequestDto.builder()
                .title("와이어프레임").content("전반적인 와이어프레임을 만들어봅시다").expiredAt(LocalDate.ofEpochDay(2023-9-05)).build();

        Card card = new Card(cardRequestDto, columns, 0);

        columns.addCard(card);

        var signupRequestDto = SignupRequestDto.builder()
                .username("su@naver.com")
                .password("password")
                .nickname("susu")
                .introduction("Hi~").build();
        String password = passwordEncoder.encode(signupRequestDto.getPassword());
        User user = new User(signupRequestDto, password, UserRoleEnum.USER);

        CardUserRequestDto cardUserRequestDto = CardUserRequestDto.builder().cardusernames(List.of("su@naver.com")).build();

        CardUser cardUser = new CardUser(card, user);
        // when
        when(cardRepository.findById(any(Long.class))).thenReturn(Optional.of(card));
        when(cardUserRepository.findAllByCard(any(Card.class))).thenReturn(null);
        when(userRepository.findByUsername(any(String.class))).thenReturn(Optional.of(user));
        cardService.updateCardUser(1L, cardUserRequestDto);

        // then
        verify(cardUserRepository, times(1)).save(any(CardUser.class));
    }

    @Test
    @DisplayName("카드 순서 이동(같은 컬럼 내에서) 성공")
    void changeCardOrder() {
        // given
        Columns columns = mock(Columns.class);
        var cardRequestDto = CardRequestDto.builder()
                .title("와이어프레임").content("전반적인 와이어프레임을 만들어봅시다").expiredAt(LocalDate.ofEpochDay(2023-9-05)).build();
        var cardRequestDto2 = CardRequestDto.builder()
                .title("와이어프레임").content("전반적인 와이어프레임을 만들어봅시다").expiredAt(LocalDate.ofEpochDay(2023-9-05)).build();

        Card card = new Card(cardRequestDto, columns, 0);
        Card card2 = new Card(cardRequestDto2, columns, 1);
        card.updateId(1L);
        card2.updateId(2L);

        columns.addCard(card);
        columns.addCard(card2);

        CardOrderRequestDto cardOrderRequestDto = CardOrderRequestDto.builder()
                .columnId(1L).cardOrder(0).build();

        // when
        when(cardRepository.findById(any(Long.class))).thenReturn(Optional.of(card2));
        when(columns.getId()).thenReturn(1L);
        when(columnsRepository.findById(any(Long.class))).thenReturn(Optional.of(columns));
        when(cardRepository.findAllByColumnsOrderByCardOrder(any(Columns.class))).thenReturn(List.of(card, card2));
        cardService.changeCardOrder(1L, cardOrderRequestDto);

        // then
        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    @DisplayName("카드 유저 확인 성공")
    void findCardUsers() {
        // given
        var boardRequestDto = BoardRequestDto.builder()
                .title("미니프로젝트")
                .content("한달 동안 미니프로젝트를 진행합니다")
                .expiredAt(LocalDate.ofEpochDay(2023-10-04)).build();
        Board board = new Board(boardRequestDto);
        Columns columns = mock(Columns.class);
        var cardRequestDto = CardRequestDto.builder()
                .title("와이어프레임").content("전반적인 와이어프레임을 만들어봅시다").expiredAt(LocalDate.ofEpochDay(2023-9-05)).build();
        Card card = new Card(cardRequestDto, columns, 0);

        BoardUser boardUser1 = boardUser(board, "su@gmail.com", "password", "susu", "Hi!");
        BoardUser boardUser2 = boardUser(board, "ye@gmail.com", "password", "yeye", "Hello");
        CardUser cardUser = cardUser(card, "su@gmail.com", "password", "susu", "Hi!");

        // when
        when(cardRepository.findById(any(Long.class))).thenReturn(Optional.of(card));
        when(cardUserRepository.findAllByCard(any(Card.class))).thenReturn(List.of(cardUser));
        when(boardUserRepository.findAllByBoard(any(Board.class))).thenReturn(List.of(boardUser1, boardUser2));
        List<BoardUserResponseDto> users = cardService.findCardUsers(board, 1L);

        // then
        assert users.get(0).getSelected().equals(true) && users.get(0).getUsername().equals(boardUser1.getUser().getUsername());
        assert users.get(1).getSelected().equals(false) && users.get(1).getUsername().equals(boardUser2.getUser().getUsername());
    }

    public CardUser cardUser(Card card, String username, String password, String nickname, String introduction) {
        var signupRequestDto = SignupRequestDto.builder()
                .username(username)
                .password(password)
                .nickname(nickname)
                .introduction(introduction).build();
        String encodepassword = passwordEncoder.encode(signupRequestDto.getPassword());
        User user = new User(signupRequestDto, encodepassword, UserRoleEnum.USER);
        return new CardUser(card, user);
    }

    public BoardUser boardUser(Board board, String username, String password, String nickname, String introduction) {
        var signupRequestDto = SignupRequestDto.builder()
                .username(username)
                .password(password)
                .nickname(nickname)
                .introduction(introduction).build();
        String encodepassword = passwordEncoder.encode(signupRequestDto.getPassword());
        User user = new User(signupRequestDto, encodepassword, UserRoleEnum.USER);
        return new BoardUser(board, user, BoardUserRoleEnum.MANAGER);
    }

}