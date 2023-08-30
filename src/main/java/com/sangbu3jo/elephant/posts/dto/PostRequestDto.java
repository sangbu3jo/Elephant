package com.sangbu3jo.elephant.posts.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class PostRequestDto {

    private String title;
    private String content;

    private Category category;

    private Boolean completed = false;
    private Integer selectNum;

}
