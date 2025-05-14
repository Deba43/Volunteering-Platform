package com.cvp.controller;

import com.cvp.dto.RatingResponseDTO;
import com.cvp.model.Rating;
import com.cvp.service.RatingService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    // Submit a new rating
    @PostMapping("/submit")
    public ResponseEntity<String> submitRating(@RequestBody Rating rating) {
        try {
            ratingService.saveRating(rating);
            return ResponseEntity.ok("Rating submitted successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ✅ Return full rating details (task name, value, review) for a given user
    @GetMapping("/rated-tasks/{userId}")
    public ResponseEntity<?> getRatedTasksWithDetails(@PathVariable String userId) {
        List<RatingResponseDTO> ratingDetails = ratingService.getRatingsByUserId(userId);

        if (ratingDetails.isEmpty()) {
            return ResponseEntity.ok("You haven't submitted any rating.");
        }

        return ResponseEntity.ok(ratingDetails);
    }

    // ✅ Return only the IDs of tasks already rated by the user
    @GetMapping("/rated-task-ids/{userId}")
    public ResponseEntity<List<String>> getRatedTaskIds(@PathVariable String userId) {
        List<String> ratedTaskIds = ratingService.getRatedTaskIdsByUserId(userId);
        return ResponseEntity.ok(ratedTaskIds);
    }

    // Return all ratings for a specific task
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<Rating>> getRatingsByTask(@PathVariable("taskId") String taskId) {
        List<Rating> ratings = ratingService.getRatingsForTask(taskId);
        return ResponseEntity.ok(ratings);
    }
}