package com.sangbu3jo.elephant.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/view")
public class AdminViewController {

    // 관리자페이지
    @GetMapping("/admins")
    public String backAdminsPage(){
        return "backAdmins";
    }

    // 사용자 정보관리 페이지
    @GetMapping("/users")
    public String backUsersPage(){
        return "backUsers";
    }

    // notification 테스트
    @GetMapping("/notification")
    public String notification(){
        return "notification";
    }
}
