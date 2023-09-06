package com.sangbu3jo.elephant.columns.service;

import com.sangbu3jo.elephant.board.entity.Board;
import com.sangbu3jo.elephant.board.repository.BoardRepository;
import com.sangbu3jo.elephant.card.dto.CardResponseDto;
import com.sangbu3jo.elephant.card.repository.CardRepository;
import com.sangbu3jo.elephant.columns.dto.ColumnsOrderRequestDto;
import com.sangbu3jo.elephant.columns.dto.ColumnsRequestDto;
import com.sangbu3jo.elephant.columns.dto.ColumnsResponseDto;
import com.sangbu3jo.elephant.columns.entity.Columns;
import com.sangbu3jo.elephant.columns.repository.ColumnsRepository;
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

    /**
     * 컬럼 생성
     * @param boardId: 컬럼을 생성할 프로젝트(보드)의 ID
     * @param columnsRequestDto: 생성할 컬럼의 제목
     * @return: 생성된 컬럼의 내용(ColumnsResponseDto)를 반환
     */
    @Transactional
    public ColumnsResponseDto createColumns(Long boardId, ColumnsRequestDto columnsRequestDto) {
        Board board = findBoard(boardId);
        Columns columns = new Columns(columnsRequestDto, board, board.getColumnsList().size());
        columnsRepository.save(columns);
        board.addColumns(columns);
        return new ColumnsResponseDto(columns);
    }

    /**
     * 컬럼 내용 수정
     * @param boardId: 수정할 컬럼을 담은 프로젝트(보드)의 ID
     * @param columnId: 수정할 컬럼의 ID
     * @param columnsRequestDto: 수정할 컬럼의 제목
     * @return: 수정된 컬럼의 내용(ColumnsResponseDto)를 반환
     */
    @Transactional
    public ColumnsResponseDto updateColumns(Long boardId, Long columnId, ColumnsRequestDto columnsRequestDto) {
        // 보드의 존재 확인
        findBoard(boardId);
        Columns columns = findColumns(columnId);

        columns.updateColumn(columnsRequestDto);
        return new ColumnsResponseDto(columns);
    }

    /**
     * 컬럼 삭제
     * @param boardId: 삭제할 칼럼을 담은 프로젝트(보드)의 ID
     * @param columnId: 삭제할 컬럼의 ID
     */
    @Transactional
    public void deleteColumns(Long boardId, Long columnId) {
        Board board = findBoard(boardId);
        Columns columns = findColumns(columnId);

        // 해당 보드의 컬럼 리스트에서 컬럼 삭제
        board.removeColumns(columns);
        // 레파지토리에서 컬럼 삭제
        columnsRepository.delete(columns);
    }

    /**
     * 컬럼 순서 변경(이동)
     * @param boardId: 이동할 컬럼을 담은 프로젝트(보드)의 ID
     * @param columnId: 이동한 컬럼의 ID
     * @param columnsOrderRequestDto: 이동한 컬럼의 위치를 받아옴
     * @return: 변경된 컬럼이 존재하는 프로젝트(보드)에 존재하는 컬럼 리스트를 반환
     */
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

    /**
     * 프로젝트(보드)에 존재하는 컬럼들 찾기
     * @param boardId: 컬럼을 가져올 프로젝트(보드)의 ID
     * @return: 프로젝트(보드)에 존재하는 컬럼의 리스트 반환
     */
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

    /**
     * 프로젝트(보드) ID로 프로젝트(보드)를 찾음
     * @param boardId: 찾을 프로젝트(보드)의 ID
     * @return: Board를 반환
     */
    public Board findBoard(Long boardId) {
        return boardRepository.findById(boardId).orElseThrow(IllegalArgumentException::new);
    }

    /**
     * 컬럼의 ID로 컬럼을 찾음
     * @param columnId: 찾을 컬럼의 ID
     * @return: Columns를 반환
     */
    private Columns findColumns(Long columnId) {
        return columnsRepository.findById(columnId).orElseThrow(IllegalArgumentException::new);
    }


}