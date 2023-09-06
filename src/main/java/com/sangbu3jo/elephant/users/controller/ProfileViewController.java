package com.sangbu3jo.elephant.users.controller;

import com.sangbu3jo.elephant.security.UserDetailsImpl;
import com.sangbu3jo.elephant.users.dto.UserResponseDto;
import com.sangbu3jo.elephant.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class ProfileViewController {


    private final UserService userService;

    @GetMapping("/users/profile/images")
    public String getImage(@AuthenticationPrincipal UserDetailsImpl userDetails,
                           Model model){

        UserResponseDto userResponseDto = userService.getUserInfo(userDetails.getUser());
        model.addAttribute("image",userResponseDto.getProfileUrl());
        return "backUsers";

    }


}
