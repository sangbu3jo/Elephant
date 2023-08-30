package com.sangbu3jo.elephant.posts.entity;

public enum Category {
    COOPERATION_PROJECT("협업 프로젝트"),
    DEVELOPMENT_STUDY("개발 스터디"),
    PREVIOUS_EXAM("문제 은행"),
    FORUM_BOARD("자유 게시판");





    private final String categoryName;

    Category(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public static String getCategory(int index){
        String categoryName = "";
        switch (index) {
            case 1:
                categoryName = Category.COOPERATION_PROJECT.getCategoryName();
                break;
            case 2:
                categoryName = Category.COOPERATION_PROJECT.getCategoryName();
                break;
            case 3:
                categoryName = Category.PREVIOUS_EXAM.getCategoryName();
                break;
            case 4:
                categoryName = Category.FORUM_BOARD.getCategoryName();
                break;
            default:
                throw new IllegalArgumentException("해당 카테고리가 존재하지 않습니다.");
        }
        return categoryName;
    }
}

