package com.cvp.frontend.service;

import com.cvp.frontend.model.Task;
import com.cvp.frontend.model.TaskSignup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskSignupService {

    private static final String BASE_URL = "http://localhost:7777/tasksignup";

    @Autowired
    private RestTemplate restTemplate;

    public String registerVolunteer(TaskSignup taskSignup) {
        ResponseEntity<String> response = restTemplate.postForEntity(
                BASE_URL + "/register",
                taskSignup,
                String.class);
        return response.getBody();
    }

    public List<TaskSignup> getAllSignups() {
        ResponseEntity<TaskSignup[]> response = restTemplate.getForEntity(BASE_URL + "/all", TaskSignup[].class);
        TaskSignup[] signups = response.getBody();
        return signups != null ? Arrays.asList(signups) : new ArrayList<>();
    }

    // Fetch available tasks from backend
    public List<Task> getAvailableTasks() {
        String url = "http://localhost:7777/task/availableTasks";
        ResponseEntity<Task[]> response = restTemplate.getForEntity(url, Task[].class);
        Task[] tasks = response.getBody();
        return tasks != null ? Arrays.asList(tasks) : new ArrayList<>();
    }

    // Return available tasks for a user by filtering out tasks the user has signed
    // up for
    public List<Task> getAvailableTasksForUser(String userId) {
        List<Task> allTasks = getAvailableTasks();
        List<TaskSignup> allSignups = getAllSignups();

        // Collect task titles that the user is already signed up for
        Set<String> signedTaskNames = allSignups.stream()
                .filter(ts -> ts.getUserId() != null && ts.getUserId().equals(userId))
                .map(TaskSignup::getTaskName)
                .collect(Collectors.toSet());

        // Filter out tasks already signed up for
        return allTasks.stream()
                .filter(task -> !signedTaskNames.contains(task.getTitle()))
                .collect(Collectors.toList());
    }

    public List<TaskSignup> getSignedUpTasksForUser(String userId) {
        String url = "http://localhost:7777/tasksignup/user/signedup/" + userId;
        ResponseEntity<TaskSignup[]> response = restTemplate.getForEntity(url, TaskSignup[].class);
        TaskSignup[] tasks = response.getBody();
        return tasks != null ? Arrays.asList(tasks) : new ArrayList<>();
    }

}
