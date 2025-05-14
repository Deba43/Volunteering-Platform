package com.cvp.service;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.cvp.dto.RatingResponseDTO;
import com.cvp.model.Rating;
import com.cvp.model.Task;
import com.cvp.repository.RatingRepository;
import com.cvp.repository.TaskRepository;
import com.cvp.repository.TaskSignupRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final TaskRepository taskRepository;
    private final TaskSignupRepository taskSignupRepository;
    private final DynamoDBMapper dynamoDBMapper;

    public RatingService(RatingRepository ratingRepository, TaskRepository taskRepository,
            TaskSignupRepository taskSignupRepository, DynamoDBMapper dynamoDBMapper) {
        this.ratingRepository = ratingRepository;
        this.taskRepository = taskRepository;
        this.taskSignupRepository = taskSignupRepository;
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public Rating saveRating(Rating rating) {
        String userId = rating.getUserId();
        String taskId = rating.getTaskId();

        // ✅ Check if task exists
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        // ✅ Check if task is completed
        if (!"COMPLETED".equals(task.getStatus())) {
            throw new IllegalStateException("Rating can only be submitted for completed tasks.");
        }

        // ✅ Check if user already rated this task
        boolean alreadyRated = ratingRepository.existsByUserIdAndTaskId(userId, taskId);
        if (alreadyRated) {
            throw new IllegalStateException("You already rated this task.");
        }

        // ✅ Check if user signed up for this task
        boolean isUserSignedUp = taskSignupRepository.findTasksByUserId(userId)
                .stream()
                .anyMatch(t -> taskId.equals(t.getTaskId()));

        if (!isUserSignedUp) {
            throw new RuntimeException("You can only rate tasks you have signed up for.");
        }

        dynamoDBMapper.save(rating);
        return rating;
    }

    public List<RatingResponseDTO> getRatingsByUserId(String userId) {
        List<Rating> ratings = ratingRepository.findAll()
                .stream()
                .filter(r -> r.getUserId().equals(userId))
                .toList();

        if (ratings.isEmpty()) {
            return List.of();
        }

        return ratings.stream()
                .map(r -> {
                    String title = taskRepository.findById(r.getTaskId())
                            .map(Task::getTitle)
                            .orElse("Deleted Task");
                    return new RatingResponseDTO(
                            title,
                            r.getRatingValue(),
                            (r.getReview() != null && !r.getReview().isBlank()) ? r.getReview() : "No review");
                })
                .toList();
    }

    public List<String> getRatedTaskIdsByUserId(String userId) {
        return ratingRepository.findRatedTaskIdsByUserId(userId);
    }

    public List<Rating> getRatingsForTask(String taskId) {
        return ratingRepository.findByTaskId(taskId);
    }
}
