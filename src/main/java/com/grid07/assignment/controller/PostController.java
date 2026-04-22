package com.grid07.assignment.controller;

import com.grid07.assignment.dto.CommentRequest;
import com.grid07.assignment.dto.LikeRequest;
import com.grid07.assignment.dto.PostRequest;
import com.grid07.assignment.entity.Bot;
import com.grid07.assignment.entity.Comment;
import com.grid07.assignment.entity.Post;
import com.grid07.assignment.entity.User;
import com.grid07.assignment.repository.BotRepository;
import com.grid07.assignment.repository.CommentRepository;
import com.grid07.assignment.repository.PostRepository;
import com.grid07.assignment.repository.UserRepository;
import com.grid07.assignment.service.NotificationService;
import com.grid07.assignment.service.RedisInteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final BotRepository botRepository;
    private final RedisInteractionService redisInteractionService;
    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody PostRequest request) {
        Post post = new Post();
        post.setAuthorId(request.getAuthorId());
        post.setContent(request.getContent());
        post.setCreatedAt(Instant.now());
        return ResponseEntity.ok(postRepository.save(post));
    }

    @PostMapping("/{postId}/comments")
    @Transactional
    public ResponseEntity<?> addComment(@PathVariable UUID postId, @RequestBody CommentRequest request) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isEmpty()) return ResponseEntity.notFound().build();
        Post post = postOpt.get();

        if (request.getDepthLevel() > 20) {
            return ResponseEntity.badRequest().body("Vertical Cap: Depth level exceeded.");
        }

        boolean isBot = botRepository.findById(request.getAuthorId()).isPresent();

        if (isBot) {
            if (!redisInteractionService.checkAndIncrementBotReply(postId)) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Horizontal Cap: 100 bot replies exceeded.");
            }

            // Cooldown Cap
            if (!redisInteractionService.checkCooldown(request.getAuthorId(), post.getAuthorId())) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Cooldown Cap: Try again after 10 minutes.");
            }
            
            redisInteractionService.addViralityScore(postId, 1);
            
            notificationService.handleBotInteraction(request.getAuthorId(), post.getAuthorId(), "Bot interacted with your post.");
        } else {
            redisInteractionService.addViralityScore(postId, 50);
        }

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setAuthorId(request.getAuthorId());
        comment.setContent(request.getContent());
        comment.setDepthLevel(request.getDepthLevel());
        comment.setCreatedAt(Instant.now());
        commentRepository.save(comment);

        return ResponseEntity.ok(comment);
    }

    @PostMapping("/{postId}/like")
    @Transactional
    public ResponseEntity<?> likePost(@PathVariable UUID postId, @RequestBody LikeRequest request) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isEmpty()) return ResponseEntity.notFound().build();

        boolean isBot = botRepository.findById(request.getUserId()).isPresent();
        if (isBot) {
            redisInteractionService.addViralityScore(postId, 1);
        } else {
            redisInteractionService.addViralityScore(postId, 20);
        }
        
        return ResponseEntity.ok().build();
    }
}
