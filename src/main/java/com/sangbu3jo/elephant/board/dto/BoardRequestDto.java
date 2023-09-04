package com.sangbu3jo.elephant.board.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Builder
@Getter
public class BoardRequestDto {

    private String title;         // 프로젝트 제목
    private String content;       // 프로젝트 설명
    private LocalDate expiredAt;  // 프로젝트 마감일
}
