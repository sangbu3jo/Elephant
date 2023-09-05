package com.sangbu3jo.elephant.chat.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ChatUserRequestDto {

    List<String> username;
}
