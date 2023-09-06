package com.sangbu3jo.elephant.board.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class BoardRequestDto {

    private String title;         // 프로젝트 제목
    private String content;       // 프로젝트 설명
    private LocalDate expiredAt;  // 프로젝트 마감일
}
