package com.sangbu3jo.elephant.posts.dto;

import com.sangbu3jo.elephant.posts.entity.Category;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostRequestDto {

    private String title;
    private String content;
    private int category;
    private String files;
    private Boolean completed = false;

    private Integer selectNum;


}
