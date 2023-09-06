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

    /**
     * 관리자 페이지를 렌더링
     *
     * @param model 뷰에 전달할 데이터를 관리하는 Spring의 Model 객체
     * @param userDetails 현재 인증된 사용자의 상세 정보
     * @return 관리자 페이지의 뷰 이름
     */
    @GetMapping("/admins")
    public String backAdminsPage(Model model,
        @AuthenticationPrincipal UserDetailsImpl userDetails){
        checkAdmin(model, userDetails);
        return "backAdmins";
    }

    /**
     * 사용자 정보 관리 페이지를 렌더링합니다.
     *
     * @param model 뷰에 전달할 데이터를 관리하는 Spring의 Model 객체
     * @param userDetails 현재 인증된 사용자의 상세 정보
     * @return 사용자 정보 관리 페이지의 뷰 이름
     */
    @GetMapping("/users")
    public String backUsersPage(Model model,
        @AuthenticationPrincipal UserDetailsImpl userDetails){
        checkAdmin(model, userDetails);
        return "backUsers";
    }

    /**
     * 알림(notification) 테스트 페이지를 렌더링합니다.
     *
     * @return 알림 테스트 페이지의 뷰 이름
     */
    @GetMapping("/notification")
    public String notification() {
        return "notification";
    }


    /**
     * 사용자가 관리자인지 확인하고, 관리자 여부를 Model에 추가합니다.
     *
     * @param model 뷰에 전달할 데이터를 관리하는 Spring의 Model 객체
     * @param userDetails 현재 인증된 사용자의 상세 정보
     */
    private void checkAdmin(Model model, UserDetailsImpl userDetails) {
        Boolean admin = false;
        if (userDetails.getUser().getRole().equals(UserRoleEnum.ADMIN)) {
            admin = true;
        }
        model.addAttribute("admin", admin);
    }


}
