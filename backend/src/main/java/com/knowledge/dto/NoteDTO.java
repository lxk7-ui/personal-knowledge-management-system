package com.knowledge.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class NoteDTO {
    private Long id;
    @NotBlank(message = "标题不能为空")
    private String title;
    private String content;
    private String summary;
    private Long categoryId;
    private Integer isTop;
    private Integer status;
    private List<Long> tagIds;
}
