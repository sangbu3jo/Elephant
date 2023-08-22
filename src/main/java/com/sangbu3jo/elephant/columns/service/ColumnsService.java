package com.sangbu3jo.elephant.columns.service;

import com.sangbu3jo.elephant.board.dto.BoardRequestDto;
import com.sangbu3jo.elephant.board.entity.Board;
import com.sangbu3jo.elephant.board.repository.BoardRepository;
import com.sangbu3jo.elephant.board.service.BoardService;
import com.sangbu3jo.elephant.card.dto.CardResponseDto;
import com.sangbu3jo.elephant.card.repository.CardRepository;
import com.sangbu3jo.elephant.columns.dto.ColumnsOrderRequestDto;
import com.sangbu3jo.elephant.columns.dto.ColumnsRequestDto;
import com.sangbu3jo.elephant.columns.dto.ColumnsResponseDto;
import com.sangbu3jo.elephant.columns.entity.Columns;
import com.sangbu3jo.elephant.columns.repository.ColumnsRepository;
import jakarta.persistence.Column;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j(topic = "컬럼 서비스")
@Service
@RequiredArgsConstructor
public class ColumnsService {

    private final ColumnsRepository columnsRepository;
    private final CardRepository cardRepository;
    private final BoardRepository boardRepository;

    // 컬럼 생성
    @Transactional
    public ColumnsResponseDto createColumns(Long boardId, ColumnsRequestDto columnsRequestDto) {
        Board board = findBoard(boardId);
        Columns columns = new Columns(columnsRequestDto, board, board.getColumnsList().size());
        columnsRepository.save(columns);
        board.addColumns(columns);
        return new ColumnsResponseDto(columns);
    }

    // 컬럼 내용 수정
    @Transactional
    public ColumnsResponseDto updateColumns(Long boardId, Long columnId, ColumnsRequestDto columnsRequestDto) {
        Board board = findBoard(boardId);
        Columns columns = findColumns(columnId);

        columns.updateColumn(columnsRequestDto);
        return new ColumnsResponseDto(columns);
    }

    // 컬럼 삭제
    public void deleteColumns(Long boardId, Long columnId) {
        Board board = findBoard(boardId);
        Columns columns = findColumns(columnId);

        // 해당 보드의 컬럼 리스트에서 컬럼 삭제
        board.removeColumns(columns);

        // 레파지토리에서 컬럼 삭제
        columnsRepository.delete(columns);
    }

    // 컬럼 이동
    @Transactional
    public List<ColumnsResponseDto> changeOrderColumns(Long boardId, Long columnId, ColumnsOrderRequestDto columnsOrderRequestDto) {
        // 컬럼 순서 변경하기
        log.info("컬럼 순서 변경 시도중");
        Board board = findBoard(boardId);
        Columns columns = findColumns(columnId);

        // 기존 column의 번호 : columns.getColumnsOrder
        // 받아온 columnsOrderRequestDto.getOrder
        List<Columns> columnsList = board.getColumnsList();

        board.changeColumns(columns, columnsOrderRequestDto.getOrder());
        log.info("순서 변경 완료");

        for (int i=0; i<columnsList.size(); i++) {
            columnsList.get(i).updateColumnOrder(i);
            columnsRepository.save(columnsList.get(i));
            log.info(columnsList.get(i).getTitle() + " 순서: " +columnsList.get(i).getColumnOrder());
        }

        boardRepository.save(board);
        return board.getColumnsList().stream().map(ColumnsResponseDto::new).toList();
    }

    public List<ColumnsResponseDto> findColumnsList(Long boardId) {
        Board board = findBoard(boardId);

        List<ColumnsResponseDto> columnsResponseDtoList = new ArrayList<>();

        List<Columns> columns = columnsRepository.findAllByBoardOrderByColumnOrder(board).stream().toList();

        for (Columns c: columns) {
            ColumnsResponseDto newC = new ColumnsResponseDto(c);
            newC.updateCardList(cardRepository.findAllByColumnsOrderByCardOrder(c).stream().map(CardResponseDto::new).toList());
            columnsResponseDtoList.add(newC);
        }

        return columnsResponseDtoList;
    }

    public Board findBoard(Long boardId) {
        return boardRepository.findById(boardId).orElseThrow(IllegalArgumentException::new);
    }


    private Columns findColumns(Long columnId) {
        return columnsRepository.findById(columnId).orElseThrow(IllegalArgumentException::new);
    }


}