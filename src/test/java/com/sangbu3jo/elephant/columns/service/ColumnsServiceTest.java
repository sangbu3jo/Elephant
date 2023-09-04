package com.sangbu3jo.elephant.columns.service;

import com.sangbu3jo.elephant.board.dto.BoardRequestDto;
import com.sangbu3jo.elephant.board.entity.Board;
import com.sangbu3jo.elephant.board.repository.BoardRepository;
import com.sangbu3jo.elephant.card.repository.CardRepository;
import com.sangbu3jo.elephant.columns.dto.ColumnsOrderRequestDto;
import com.sangbu3jo.elephant.columns.dto.ColumnsRequestDto;
import com.sangbu3jo.elephant.columns.dto.ColumnsResponseDto;
import com.sangbu3jo.elephant.columns.entity.Columns;
import com.sangbu3jo.elephant.columns.repository.ColumnsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ColumnsServiceTest {

    @InjectMocks
    ColumnsService columnsService;

    @Mock
    ColumnsRepository columnsRepository;
    @Mock
    CardRepository cardRepository;
    @Mock
    BoardRepository boardRepository;
    @Mock
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void init() {
        this.columnsService = new ColumnsService(
                this.columnsRepository = columnsRepository,
                this.cardRepository = cardRepository,
                this.boardRepository = boardRepository
        );
    }

    @Test
    @DisplayName("칼럼 생성 성공")
    void createColumns() {
        // given
        var boardRequestDto = BoardRequestDto.builder()
                .title("미니프로젝트")
                .content("한달 동안 미니프로젝트를 진행합니다")
                .expiredAt(LocalDate.ofEpochDay(2023-10-04)).build();
        Board board = new Board(boardRequestDto);

        var columnsRequestDto = ColumnsRequestDto.builder()
                .title("To Do").build();
        Columns columns = new Columns(columnsRequestDto, board, 1);

        // when
        when(boardRepository.findById(any(Long.class))).thenReturn(Optional.of(board));
        when(columnsRepository.save(any(Columns.class))).thenReturn(columns);
        ColumnsResponseDto columnsResponseDto = columnsService.createColumns(1L, columnsRequestDto);

        // then
        assert columnsResponseDto.getTitle().equals(columns.getTitle());
//        assert columnsResponseDto.getOrder() == Long.valueOf(1);
    }

    @Test
    @DisplayName("칼럼 수정 성공")
    void updateColumns() {
        // given
        var boardRequestDto = BoardRequestDto.builder()
                .title("미니프로젝트")
                .content("한달 동안 미니프로젝트를 진행합니다")
                .expiredAt(LocalDate.ofEpochDay(2023-10-04)).build();
        Board board = new Board(boardRequestDto);

        var columnsRequestDto = ColumnsRequestDto.builder()
                .title("To Do").build();
        Columns columns = new Columns(columnsRequestDto, board, 1);
        var updateColumnsRequestDto = ColumnsRequestDto.builder()
                .title("해야 할 일").build();

        // when
        when(boardRepository.findById(any(Long.class))).thenReturn(Optional.of(board));
        when(columnsRepository.findById(any(Long.class))).thenReturn(Optional.of(columns));
        ColumnsResponseDto columnsResponseDto = columnsService.updateColumns(1L, 1L, updateColumnsRequestDto);

        // then
        assert columnsResponseDto.getTitle().equals(updateColumnsRequestDto.getTitle());
    }

    @Test
    @DisplayName("컬럼 삭제 성공")
    void deleteColumns() {
        // given
        var boardRequestDto = BoardRequestDto.builder()
                .title("미니프로젝트")
                .content("한달 동안 미니프로젝트를 진행합니다")
                .expiredAt(LocalDate.ofEpochDay(2023-10-04)).build();
        Board board = new Board(boardRequestDto);

        var columnsRequestDto = ColumnsRequestDto.builder()
                .title("To Do").build();
        Columns columns = new Columns(columnsRequestDto, board, 1);
        board.addColumns(columns);

        // when
        when(boardRepository.findById(any(Long.class))).thenReturn(Optional.of(board));
        when(columnsRepository.findById(any(Long.class))).thenReturn(Optional.of(columns));
        columnsService.deleteColumns(1L, 1L);

        // then
        verify(columnsRepository, times(1)).delete(any(Columns.class));
    }

    @Test
    @DisplayName("컬럼 이동 성공")
    void changeColumnsOrder() {
        // given
        var boardRequestDto = BoardRequestDto.builder()
                .title("미니프로젝트")
                .content("한달 동안 미니프로젝트를 진행합니다")
                .expiredAt(LocalDate.ofEpochDay(2023-10-04)).build();
        Board board = new Board(boardRequestDto);

        var columnsRequestDto = ColumnsRequestDto.builder()
                .title("To Do").build();
        Columns columns = new Columns(columnsRequestDto, board, 0);
        var columnsRequestDto2 = ColumnsRequestDto.builder()
                .title("Doing").build();
        Columns columns2 = new Columns(columnsRequestDto2, board, 1);
        board.addColumns(columns);
        board.addColumns(columns2);
        var columnsOrderRequestDto = ColumnsOrderRequestDto.builder().order(1).build();

        // when
        when(boardRepository.findById(any(Long.class))).thenReturn(Optional.of(board));
        when(boardRepository.save(any(Board.class))).thenReturn(board);
        when(columnsRepository.findById(any(Long.class))).thenReturn(Optional.of(columns));

        List<ColumnsResponseDto> columnsResponseDtoList = columnsService.changeOrderColumns(1L, 1L, columnsOrderRequestDto);

        // then
        assert columns.getColumnOrder().equals(Long.valueOf(1));
        assert columns2.getColumnOrder().equals(Long.valueOf(0));
    }

}