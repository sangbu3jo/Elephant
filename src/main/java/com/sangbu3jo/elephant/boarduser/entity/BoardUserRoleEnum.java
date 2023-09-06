package com.sangbu3jo.elephant.boarduser.entity;

public enum BoardUserRoleEnum {
    MEMBER(Authority.MEMBER),    // 참여자 (프로젝트 참여자)
    MANAGER(Authority.MANAGER);  // 관리자 (프로젝트 생성자)

    private final String authority;

    BoardUserRoleEnum(String authority) {
        this.authority = authority;
    }

    public String getAuthority() {
        return this.authority;
    }

    public static class Authority {
        public static final String MEMBER = "ROLE_MEMBER";
        public static final String MANAGER = "ROLE_MANAGER";
    }
}