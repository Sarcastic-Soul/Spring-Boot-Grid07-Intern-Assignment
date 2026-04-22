# Grid07 Backend Engineering Assignment

This is a robust, high-performance Spring Boot microservice that acts as a central API gateway and guardrail system. It handles concurrent requests, manages distributed state using Redis, and implements event-driven scheduling.

## Tech Stack
- Java 17+
- Spring Boot 3.x
- PostgreSQL
- Redis (Spring Data Redis)
- Docker Compose

## Phase 1: Setup
- Spun up PostgreSQL and Redis containers using Docker Compose.
- Connected the Spring Boot application using Spring Data JPA and Spring Data Redis.
- Created `User`, `Bot`, `Post`, and `Comment` JPA Entities mapping.

## Phase 2: Redis Virality Engine & Atomic Locks
To strictly enforce limits and maintain data consistency without locking the PostgreSQL database unnecessarily, **Redis Atomic Operations** were heavily utilized:

1. **Horizontal Cap (Max 100 bot replies per post)**
   - Utilizes `redisTemplate.opsForValue().increment(key)`. Since `INCR` is an atomic operation in Redis, it avoids race conditions (e.g., 200 bots trying to comment at the exact same millisecond).
   - If the counter exceeds 100, the microservice immediately performs a decrement `DECR` to roll back the attempt and rejects the request with HTTP 429 Too Many Requests. This guarantees the count strictly stops at 100 and no extra database rows are created.

2. **Vertical Cap (Thread Depth)**
   - Simple check of `depthLevel <= 20` evaluated at the controller level before database inserts.

3. **Cooldown Cap (Bot/Human Interaction limits)**
   - Uses `setIfAbsent(key, "1", Duration.ofMinutes(10))`. This maps to Redis' `SETNX` (Set if Not eXists) combined with a TTL. It atomically sets the lock and returns true only if the interaction hasn't happened in the last 10 minutes.

## Phase 3: The Notification Engine (Smart Batching)
- To prevent spam, notifications for bot interactions push an item to a Redis List (`user:{id}:pending_notifs`).
- The 15-minute cooldown is also handled via an atomic TTL key (`user:{id}:notif_cooldown`).
- A Spring `@Scheduled` cron job runs every 5 minutes. It scans `user:*:pending_notifs` utilizing `redisTemplate.keys()`, processes the batch by logging a summarized message, and clears the list seamlessly.

## Getting Started

1. Start databases via Docker Compose:
   ```bash
   docker-compose up -d
   ```
2. Build and run the Spring Boot Application:
   ```bash
   ./mvnw spring-boot:run
   ```
3. Import the `Grid07_Assignment_Postman.json` into Postman to test the APIs. Note: You will need to create some users, bots, and posts in your database manually to fulfill the UUID foreign key relations, then populate Postman Variables (`user_id`, `bot_id`, `post_id`) respectively.

## Assumptions
- I used UUID instead of Longs or Ints for primary keys. This ensures easy polymorphic association between `author_id` and whether the author is a `Bot` or a `User` since they reside in separate tables but can both map to an `authorId` cleanly.
