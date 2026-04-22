package com.grid07.assignment.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private UUID authorId; // Can be User or Bot
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    private Instant createdAt = Instant.now();
}
