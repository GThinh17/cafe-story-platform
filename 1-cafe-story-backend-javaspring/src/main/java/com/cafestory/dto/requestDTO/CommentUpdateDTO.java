package com.cafestory.dto.requestDTO;

import com.cafestory.entity.enums.PostStatus;
import lombok.Data;

import java.util.List;

@Data
public class CommentUpdateDTO {
    private String content;
    private List<String> imageUrls;
    private PostStatus status;
}
