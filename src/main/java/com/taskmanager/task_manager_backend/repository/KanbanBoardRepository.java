package com.taskmanager.task_manager_backend.repository;

import com.taskmanager.task_manager_backend.model.KanbanBoard;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KanbanBoardRepository extends MongoRepository<KanbanBoard, String> {

    /**
     * Find a specific board by ID and username (for security)
     */
    Optional<KanbanBoard> findByUsernameAndId(String username, String boardId);

    /**
     * Find all boards for a specific user
     */
    List<KanbanBoard> findByUsername(String username);

    /**
     * Find boards by username ordered by creation date
     */
    List<KanbanBoard> findByUsernameOrderByCreatedAtDesc(String username);

    /**
     * Find boards by username ordered by update date (most recently used)
     */
    List<KanbanBoard> findByUsernameOrderByUpdatedAtDesc(String username);

    /**
     * Find board by name and username
     */
    Optional<KanbanBoard> findByUsernameAndName(String username, String name);

    /**
     * Delete a board by ID and username (for security)
     */
    void deleteByIdAndUsername(String id, String username);

    /**
     * Count boards for a user
     */
    long countByUsername(String username);

    /**
     * Check if board exists for user
     */
    boolean existsByUsernameAndId(String username, String id);
}