package com.grid07.assignment.repository;
import com.grid07.assignment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface CommentRepository extends JpaRepository<Comment, UUID> {}
