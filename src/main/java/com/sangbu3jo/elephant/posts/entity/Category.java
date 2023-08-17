package com.sangbu3jo.elephant.posts.entity;

public enum Category {
    COOPERATION_PROJECT("협업 프로젝트"),
    DEVELOPMENT_STUDY("개발 스터디"),
    PREVIOUS_EXAM("문제 은행");

    private final String categoryName;

    Category(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }
}

