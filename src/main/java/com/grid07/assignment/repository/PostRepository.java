package com.grid07.assignment.repository;
import com.grid07.assignment.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface PostRepository extends JpaRepository<Post, UUID> {}
