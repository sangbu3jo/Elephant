package com.sangbu3jo.elephant.posts.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostRequestDto {

    private String title;
    private String content;
    private String category;
    private String files;
    private Boolean completed = false;

    private Integer selectNum;


}
