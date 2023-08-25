package com.sangbu3jo.elephant.users.dto;

import com.sangbu3jo.elephant.posts.entity.Category;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserPostsDto {
    private String title;
    private String content;
    private Boolean completed;

}
