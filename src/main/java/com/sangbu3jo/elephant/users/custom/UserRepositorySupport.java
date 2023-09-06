package com.sangbu3jo.elephant.users.custom;

import com.sangbu3jo.elephant.boarduser.dto.BoardUserResponseDto;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface UserRepositorySupport {

    Slice<BoardUserResponseDto> findByUsernameOrNickname(String search, Pageable pageable);
}
