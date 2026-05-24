package com.rbhu.task_analytics.repository;

import com.rbhu.task_analytics.TaskAnalyticsApplication;
import com.rbhu.task_analytics.model.Task;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByCategory(String category);
    List<Task> findByStatus(Task.TaskStatus status);
    List<Task> findByCategoryAndStatus(String category, Task.TaskStatus status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = 'completed'")
    long countCompleted();

    @Query("SELECT t.category, COUNT(t) FROM Task t GROUP BY t.category")
    List<Object[]> countByCategory();

    @Query("SELECT t FROM Task t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Task> findByTitleContainingIgnoreCase(@Param("keyword") String keyword);

    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.status = 'completed' WHERE t.category = :category")
    int markAllCompletedByCategory(@Param("category") String category);

}
