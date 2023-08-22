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
@RequestMapping("/api")
@RequiredArgsConstructor
public class ColumnsController {

    private final ColumnsService columnsService;

    @PostMapping("/boards/{board_id}/columns")
    @ResponseBody
    public ResponseEntity<ColumnsResponseDto> createColumns(@PathVariable Long board_id,
                                                            @RequestBody ColumnsRequestDto columnsRequestDto) {
        ColumnsResponseDto columnsResponseDto = columnsService.createColumns(board_id, columnsRequestDto);
        return ResponseEntity.ok().body(columnsResponseDto);
    }

    @PutMapping("/boards/{board_id}/columns/{column_id}")
    @ResponseBody
    public ResponseEntity<ColumnsResponseDto> updateColumns(@PathVariable Long board_id,
                                                            @PathVariable Long column_id,
                                                            @RequestBody ColumnsRequestDto columnsRequestDto) {
        ColumnsResponseDto columnsResponseDto = columnsService.updateColumns(board_id, column_id,columnsRequestDto);
        return ResponseEntity.ok().body(columnsResponseDto);
    }

    @DeleteMapping("/boards/{board_id}/columns/{column_id}")
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

    // 컬럼 순서 변경
    @PatchMapping("/boards/{board_id}/columns/{column_id}")
    @ResponseBody
    public ResponseEntity<List<ColumnsResponseDto>> changeOrderColumns(@PathVariable Long board_id,
                                                                      @PathVariable Long column_id,
                                                                      @RequestBody ColumnsOrderRequestDto columnsOrderRequestDto) {
        log.info("컬럼 순서 수정");
        List<ColumnsResponseDto> columnsResponseDto = columnsService.changeOrderColumns(board_id, column_id, columnsOrderRequestDto);
        return ResponseEntity.ok().body(columnsResponseDto);
    }
    
    
}
