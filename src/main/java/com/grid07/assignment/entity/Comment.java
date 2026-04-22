package com.grid07.assignment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private UUID postId;
    private UUID authorId;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    private int depthLevel;
    
    private Instant createdAt = Instant.now();
}
