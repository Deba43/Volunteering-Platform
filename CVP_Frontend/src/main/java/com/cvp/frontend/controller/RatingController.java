package com.cvp.frontend.controller;

import com.cvp.frontend.model.Rating;
import com.cvp.frontend.model.Task;
import com.cvp.frontend.model.TaskSignup;
import com.cvp.frontend.model.User;
import com.cvp.frontend.service.RatingService;
import com.cvp.frontend.service.TaskService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/ratings")
public class RatingController {

    private final RatingService ratingService;
    private final TaskService taskService;

    public RatingController(RatingService ratingService, TaskService taskService) {
        this.ratingService = ratingService;
        this.taskService = taskService;
    }

    // Show only completed signed-up tasks
    @GetMapping("/form")
    public String showTaskList(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null)
            return "redirect:/auth/volunteers/login";

        List<Task> userTasks = taskService.getSignedUpTasksForUser(user.getId());
        List<String> ratedTaskIds = ratingService.getRatedTaskIdsByUser(user.getId());

        // Filter for only COMPLETED tasks that are not already rated
        List<Task> eligibleTasks = userTasks.stream()
                .filter(task -> "COMPLETED".equalsIgnoreCase(task.getStatus()))
                .filter(task -> !ratedTaskIds.contains(task.getId()))
                .toList();

        model.addAttribute("tasks", eligibleTasks);

        if (eligibleTasks.isEmpty()) {
            model.addAttribute("error", "No unrated completed tasks available.");
        }

        return "rating-list";
    }

    // Show the rating form only for completed tasks
    @GetMapping("/task/{taskId}/rate")
    public String showRatingForm(@PathVariable String taskId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null)
            return "redirect:/auth/volunteers/login";

        List<Task> userTasks = taskService.getSignedUpTasksForUser(user.getId());

        // Check task is both signed-up and completed
        Task task = userTasks.stream()
                .filter(t -> taskId.equals(t.getId()) && "COMPLETED".equalsIgnoreCase(t.getStatus()))
                .findFirst()
                .orElse(null);

        if (task == null) {
            model.addAttribute("error", "Task is either not completed or not accessible.");
            return "error";
        }

        Rating rating = new Rating();
        rating.setTaskId(task.getId());
        rating.setUserId(user.getId());

        model.addAttribute("task", task);
        model.addAttribute("rating", rating);
        return "rate-task";
    }

    @GetMapping("/my-ratings")
    public String showUserRatings(Model model, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null)
            return "redirect:/auth/volunteers/login";

        List<Map<String, Object>> ratings = ratingService.getDetailedRatingsByUser(user.getId());

        if (ratings == null || ratings.isEmpty()) {
            model.addAttribute("message", "You haven't submitted any rating.");
        } else {
            model.addAttribute("ratings", ratings);
        }

        model.addAttribute("user", user);

        return "my-ratings";
    }

    // Submit the rating to backend
    @PostMapping("/submit")
    public String submitRating(@ModelAttribute Rating rating, HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null)
            return "redirect:/auth/volunteers/login";

        rating.setUserId(user.getId());

        try {
            ratingService.submitRating(rating);
            model.addAttribute("message", "Rating submitted successfully!");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }

        model.addAttribute("user", user);
        return "redirect:/ratings/my-ratings";

    }
}
