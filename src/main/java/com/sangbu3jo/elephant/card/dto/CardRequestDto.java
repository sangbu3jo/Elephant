package com.sangbu3jo.elephant.card.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class CardRequestDto {

    private String title;
    private String content;
    private LocalDate expiredAt;
}
