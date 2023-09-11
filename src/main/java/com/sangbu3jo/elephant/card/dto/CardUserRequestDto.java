package com.sangbu3jo.elephant.card.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CardUserRequestDto {

    private List<String> cardusernames;

}
