package com.sangbu3jo.elephant.card.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CardOrderRequestDto {

    private Long columnId;

    private Integer cardOrder;

}
