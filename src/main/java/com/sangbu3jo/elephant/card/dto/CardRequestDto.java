package com.sangbu3jo.elephant.card.dto;


import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CardRequestDto {

    private String title;
    private String content;
    private LocalDate expiredAt;
}
