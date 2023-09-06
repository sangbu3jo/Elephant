package com.sangbu3jo.elephant.columns.controller;

import com.sangbu3jo.elephant.columns.dto.ColumnsOrderRequestDto;
import com.sangbu3jo.elephant.columns.dto.ColumnsRequestDto;
import com.sangbu3jo.elephant.columns.dto.ColumnsResponseDto;
import com.sangbu3jo.elephant.columns.service.ColumnsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j(topic = "컬럼 컨트롤러")
@Controller
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class ColumnsController {

    private final ColumnsService columnsService;

    /**
     * 컬럼 생성
     * @param board_id: URL에 매핑되어 있는 프로젝트(보드)의 ID 값
     * @param columnsRequestDto: 컬럼의 제목을 받아옴
     * @return: 생성된 컬럼에 대한 내용(ColumnsResponseDto)와 상태코드 반환
     */
    @PostMapping("/{board_id}/columns")
    @ResponseBody
    public ResponseEntity<ColumnsResponseDto> createColumns(@PathVariable Long board_id,
                                                            @RequestBody ColumnsRequestDto columnsRequestDto) {
        ColumnsResponseDto columnsResponseDto = columnsService.createColumns(board_id, columnsRequestDto);
        return ResponseEntity.ok().body(columnsResponseDto);
    }

    /**
     * 컬럼 수정
     * @param board_id: URL에 매핑되어 있는 프로젝트(보드)의 ID 값
     * @param column_id: URL에 매핑되어 있는 수정할 컬럼의 ID 값
     * @param columnsRequestDto: 수정할 컬럼의 제목을 받아옴
     * @return: 수정된 컬럼에 대한 내용(ColumnsResponseDto)와 상태코드 반환
     */
    @PutMapping("/{board_id}/columns/{column_id}")
    @ResponseBody
    public ResponseEntity<ColumnsResponseDto> updateColumns(@PathVariable Long board_id,
                                                            @PathVariable Long column_id,
                                                            @RequestBody ColumnsRequestDto columnsRequestDto) {
        ColumnsResponseDto columnsResponseDto = columnsService.updateColumns(board_id, column_id,columnsRequestDto);
        return ResponseEntity.ok().body(columnsResponseDto);
    }

    /**
     * 컬럼 삭제
     * @param board_id: URL에 매핑되어 있는 프로젝트(보드)의 ID 값
     * @param column_id: URL에 매핑되어 있는 삭제할 컬럼의 ID 값
     * @return: 메세지와 상태코드 반환
     */
    @DeleteMapping("/{board_id}/columns/{column_id}")
    @ResponseBody
    public ResponseEntity<String> deleteColumns(@PathVariable Long board_id,
                                                @PathVariable Long column_id) {
        try {
            columnsService.deleteColumns(board_id, column_id);
            return ResponseEntity.ok().body("컬럼 삭제 완료");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("컬럼이 존재하지 않습니다");
        }
    }

    /**
     * 컬럼 순서 변경(이동)
     * @param board_id: URL에 매핑되어 있는 프로젝트(보드)의 ID 값
     * @param column_id: URL에 매핑되어 있는 이동할 컬럼의 ID 값
     * @param columnsOrderRequestDto: 변경할 컬럼의 순서를 받아옴
     * @return: 변경된 컬럼이 포함된 프로젝트(보드)에 존재하는 컬럼의 리스트와 상태코드 반환
     */
    @PatchMapping("/{board_id}/columns/{column_id}")
    @ResponseBody
    public ResponseEntity<List<ColumnsResponseDto>> changeOrderColumns(@PathVariable Long board_id,
                                                                      @PathVariable Long column_id,
                                                                      @RequestBody ColumnsOrderRequestDto columnsOrderRequestDto) {
        log.info("컬럼 순서 수정");
        List<ColumnsResponseDto> columnsResponseDto = columnsService.changeOrderColumns(board_id, column_id, columnsOrderRequestDto);
        return ResponseEntity.ok().body(columnsResponseDto);
    }
    
    
}
