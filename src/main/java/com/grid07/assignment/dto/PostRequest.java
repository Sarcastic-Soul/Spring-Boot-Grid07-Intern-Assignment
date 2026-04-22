package com.grid07.assignment.dto;
import lombok.Data;
import java.util.UUID;
@Data
public class PostRequest {
    private UUID authorId;
    private String content;
}
