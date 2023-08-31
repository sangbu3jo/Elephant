package com.sangbu3jo.elephant.posts.dto;

import com.sangbu3jo.elephant.posts.entity.Category;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostRequestDto {

    @NotEmpty(message = "제목을 입력해주세요.")
    private String title;

    @NotEmpty(message = "게시글을 입력해주세요.")
    private String content;

    private Category category;

    private Boolean completed = false;
    private Integer selectNum;

}
