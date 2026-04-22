package com.grid07.assignment.repository;
import com.grid07.assignment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface UserRepository extends JpaRepository<User, UUID> {}
