package com.sangbu3jo.elephant.admin.controller;

import com.sangbu3jo.elephant.security.UserDetailsImpl;
import com.sangbu3jo.elephant.users.entity.UserRoleEnum;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/view")
public class AdminViewController {

    // 관리자페이지
    @GetMapping("/admins")
    public String backAdminsPage(Model model,
        @AuthenticationPrincipal UserDetailsImpl userDetails){
        Boolean admin = false;
        if (userDetails.getUser().getRole().equals(UserRoleEnum.ADMIN)) {
            admin = true;
        }
        model.addAttribute("admin", admin);
        return "backAdmins";
    }


    // 사용자 정보관리 페이지
    @GetMapping("/users")
    public String backUsersPage(Model model,
        @AuthenticationPrincipal UserDetailsImpl userDetails){
        Boolean admin = false;
        if (userDetails.getUser().getRole().equals(UserRoleEnum.ADMIN)) {
            admin = true;
        }
        model.addAttribute("admin", admin);
        return "backUsers";
    }

    // notification 테스트
    @GetMapping("/notification")
    public String notification() {
        return "notification";
    }

}
