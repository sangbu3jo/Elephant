package com.sangbu3jo.elephant.card.dto;


import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CardUserRequestDto {

    private List<String> cardusernames;

}
