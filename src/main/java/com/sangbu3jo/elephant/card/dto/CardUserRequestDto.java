package com.sangbu3jo.elephant.card.dto;


import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardUserRequestDto {

    private List<String> cardusernames;

}
