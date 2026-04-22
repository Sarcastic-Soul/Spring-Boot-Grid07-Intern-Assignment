package com.grid07.assignment.repository;
import com.grid07.assignment.entity.Bot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface BotRepository extends JpaRepository<Bot, UUID> {}
