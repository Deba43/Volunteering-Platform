package com.cvp.controller;

import com.cvp.service.TaskSignupService;
import com.cvp.model.TaskSignup;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasksignup")
public class TaskSignupController {

    private final TaskSignupService taskSignupService;

    public TaskSignupController(TaskSignupService taskSignupService) {
        this.taskSignupService = taskSignupService;
    }

    @GetMapping("/all")
    public List<TaskSignup> getAllSignups() {
        return taskSignupService.getAllSignups();
    }

    @PostMapping("/register")
    public String registerTask(@Valid @RequestBody TaskSignup taskSignup, BindingResult result) {
        if (result.hasErrors()) {
            return result.getFieldErrors().get(0).getDefaultMessage(); // Returns the first validation error message
        }

        return taskSignupService.registerForTask(taskSignup);
    }

    @GetMapping("/user/signedup/{userId}")
    public ResponseEntity<List<TaskSignup>> getSignedUpTasks(@PathVariable String userId) {
        List<TaskSignup> signedUpTasks = taskSignupService.getSignedUpTasksByUserId(userId);
        return ResponseEntity.ok(signedUpTasks);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaskSignup>> getUserTasks(@PathVariable String userId) {
        List<TaskSignup> signedUpTasks = taskSignupService.getSignedUpTasksByUserId(userId);
        return ResponseEntity.ok(signedUpTasks);
    }
}
