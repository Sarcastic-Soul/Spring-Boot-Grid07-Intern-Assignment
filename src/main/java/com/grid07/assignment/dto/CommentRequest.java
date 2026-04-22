package com.grid07.assignment.dto;
import lombok.Data;
import java.util.UUID;
@Data
public class CommentRequest {
    private UUID authorId;
    private String content;
    private int depthLevel;
}
