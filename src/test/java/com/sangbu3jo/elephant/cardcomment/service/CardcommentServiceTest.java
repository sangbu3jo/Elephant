package com.sangbu3jo.elephant.cardcomment.service;

import com.sangbu3jo.elephant.auth.dto.SignupRequestDto;
import com.sangbu3jo.elephant.card.dto.CardRequestDto;
import com.sangbu3jo.elephant.card.entity.Card;
import com.sangbu3jo.elephant.card.repository.CardRepository;
import com.sangbu3jo.elephant.cardcomment.dto.CardcommentRequestDto;
import com.sangbu3jo.elephant.cardcomment.dto.CardcommentResponseDto;
import com.sangbu3jo.elephant.cardcomment.entity.CardComment;
import com.sangbu3jo.elephant.cardcomment.repository.CardcommentRepository;
import com.sangbu3jo.elephant.columns.entity.Columns;
import com.sangbu3jo.elephant.users.entity.User;
import com.sangbu3jo.elephant.users.entity.UserRoleEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardcommentServiceTest {

    @InjectMocks
    CardcommentService cardcommentService;

    @Mock
    CardcommentRepository cardcommentRepository;

    @Mock
    CardRepository cardRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void init() {
        this.cardcommentService = new CardcommentService(
                this.cardcommentRepository = cardcommentRepository,
                this.cardRepository = cardRepository
        );
    }

    @Test
    @DisplayName("카드 댓글 작성 성공")
    void createCardComment() {
        // given
        User user = user("su@naver.com", "password", "susu","Hi?");
        Card card = card();
        var cardcommentRequestDto = CardcommentRequestDto.builder().content("가능하신가요?").build();

        // when
        when(cardRepository.findById(any(Long.class))).thenReturn(Optional.of(card));
        CardcommentResponseDto cardcommentResponseDto = cardcommentService.createCardComment(user, 1L, cardcommentRequestDto);

        // then
        assert cardcommentResponseDto.getContent().equals(cardcommentRequestDto.getContent());
        assert cardcommentResponseDto.getUsername().equals(user.getUsername());
    }

    @Test
    @DisplayName("카드 댓글 수정 성공")
    void updateCardComment() {
        // given
        User user = user("su@naver.com", "password", "susu","Hi?");
        Card card = card();
        var cardcommentRequestDto = CardcommentRequestDto.builder().content("가능하신가요?").build();
        CardComment comment = cardCommment(cardcommentRequestDto, card, user);
        var updatecardcommentRequestDto = CardcommentRequestDto.builder().content("파이팅 해봅시다!").build();

        // when
        when(cardcommentRepository.findById(any(Long.class))).thenReturn(Optional.of(comment));
        CardcommentResponseDto cardcommentResponseDto = cardcommentService.updateCardComment(user, 1L, updatecardcommentRequestDto);

        // then
        assert cardcommentResponseDto.getUsername().equals(user.getUsername());
        assert cardcommentResponseDto.getContent().equals(updatecardcommentRequestDto.getContent());
    }

    @Test
    @DisplayName("카드 댓글 삭제 성공")
    void deleteCardComment() {
        // given
        User user = user("su@naver.com", "password", "susu","Hi?");
        Card card = card();
        var cardcommentRequestDto = CardcommentRequestDto.builder().content("가능하신가요?").build();
        CardComment comment = cardCommment(cardcommentRequestDto, card, user);

        // when
        when(cardcommentRepository.findById(any(Long.class))).thenReturn(Optional.of(comment));
        cardcommentService.deleteCardComment(user, 1L);

        // then
        verify(cardcommentRepository, times(1)).delete(any(CardComment.class));
    }

    @Test
    @DisplayName("카드 댓글 삭제 실패")
    void deleteCardCommentFail() {
        // given
        User user = user("su@naver.com", "password", "susu","Hi?");
        User user2 = user("ye@gmail.com", "password", "yeye","Highlight");
        Card card = card();
        var cardcommentRequestDto = CardcommentRequestDto.builder().content("가능하신가요?").build();
        CardComment comment = cardCommment(cardcommentRequestDto, card, user);

        // when
        when(cardcommentRepository.findById(any(Long.class))).thenReturn(Optional.of(comment));

        try {
            cardcommentService.deleteCardComment(user2, 1L);
            fail("AccessDeniedException이 던져지지 않았습니다.");
        } catch (AccessDeniedException e) {
            assert "작성자만 삭제 가능합니다".equals(e.getMessage());
        }
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

    public Card card() {
        Columns columns = mock(Columns.class);
        var cardRequestDto = CardRequestDto.builder()
                .title("와이어프레임").content("전반적인 와이어프레임을 만들어봅시다").expiredAt(LocalDate.ofEpochDay(2023-9-05)).build();
        return new Card(cardRequestDto, columns, 0);
    }

    public CardComment cardCommment(CardcommentRequestDto cardcommentRequestDto, Card card, User user) {
        return new CardComment(cardcommentRequestDto, card, user);
    }

}