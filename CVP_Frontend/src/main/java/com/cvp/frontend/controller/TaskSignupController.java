package com.cvp.frontend.controller;

import com.cvp.frontend.model.Task;
import com.cvp.frontend.model.TaskSignup;
import com.cvp.frontend.model.User;
import com.cvp.frontend.service.TaskSignupService;
import jakarta.servlet.http.HttpSession;

import java.util.*;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

@Controller
@RequestMapping("/tasksignup")
public class TaskSignupController {

    private final TaskSignupService taskSignupService;

    public TaskSignupController(TaskSignupService taskSignupService) {
        this.taskSignupService = taskSignupService;

    }

    // Display Task Signup Form
    @GetMapping("/form")
    public String showTaskSignupForm(Model model, HttpSession session) {
        model.addAttribute("taskSignup", new TaskSignup());
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser != null) {
            String volunteerName = loggedInUser.getFirstName() + " " + loggedInUser.getLastName();
            model.addAttribute("volunteerName", volunteerName);
            // Use the optimized method to fetch only those tasks the user hasn't signed up
            // for.
            List<Task> availableTasks = taskSignupService.getAvailableTasksForUser(loggedInUser.getId());
            model.addAttribute("availableTasks", availableTasks != null ? availableTasks : new ArrayList<>());
        } else {
            System.out.println("No logged in user found in session.");
            model.addAttribute("availableTasks", taskSignupService.getAvailableTasks());
        }
        return "task-signup"; // Thymeleaf template: task-signup.html
    }

    // Process Task Signup Submission
    @PostMapping("/register")
    public String registerTaskSignup(@ModelAttribute("taskSignup") TaskSignup taskSignup,
            BindingResult result, Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            result.reject("error", "You must be logged in to sign up for a task.");
            model.addAttribute("availableTasks", taskSignupService.getAvailableTasks());
            return "task-signup";
        }
        taskSignup.setUserId(loggedInUser.getId());
        // Set volunteer name on the TaskSignup object
        String volunteerName = loggedInUser.getFirstName() + " " + loggedInUser.getLastName();
        taskSignup.setVolunteerName(volunteerName);
        try {
            String message = taskSignupService.registerVolunteer(taskSignup);
            model.addAttribute("message", message);
            System.out.println("Task Signup response: " + message);
        } catch (HttpClientErrorException e) {
            System.out.println("Error during task signup: " + e.getMessage());
            result.reject("error", "Registration failed: " + e.getMessage());
        } catch (Exception ex) {
            System.out.println("Unexpected error during task signup: " + ex.getMessage());
            ex.printStackTrace();
            result.reject("error", "Registration failed: " + ex.getMessage());
        }
        // After registration, re-populate the volunteer name and available tasks
        model.addAttribute("volunteerName", volunteerName);
        model.addAttribute("availableTasks", taskSignupService.getAvailableTasksForUser(loggedInUser.getId()));
        // Reset the form
        model.addAttribute("taskSignup", new TaskSignup());
        return "task-signup";
    }

    @GetMapping("/user/mytasks")
    public String viewUserSignedUpTasks(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        // Show empty state for non-logged in users instead of redirect
        if (loggedInUser == null) {
            model.addAttribute("errorMessage", "Please login to view your tasks");
            return "user-no-tasks";
        }

        List<TaskSignup> signedUpTasks = taskSignupService.getSignedUpTasksForUser(loggedInUser.getId());

        if (signedUpTasks.isEmpty()) {
            model.addAttribute("errorMessage", "No tasks found");
            return "user-no-tasks";
        }

        model.addAttribute("signedUpTasks", signedUpTasks);
        return "user-signedup-tasks";
    }

    @GetMapping("/view")
    public String viewSignups(Model model) {
        model.addAttribute("signups", taskSignupService.getAllSignups());
        System.out.println("Retrieved " + taskSignupService.getAllSignups().size() + " task signups.");
        return "view-signups"; // Thymeleaf template: view-signups.html
    }
}
