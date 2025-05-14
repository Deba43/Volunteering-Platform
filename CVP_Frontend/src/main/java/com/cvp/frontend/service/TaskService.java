package com.cvp.frontend.service;

import com.cvp.frontend.model.Task;
import com.cvp.frontend.model.TaskSignup;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class TaskService {

    private final RestTemplate restTemplate;

    public TaskService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Get tasks the user has signed up for (used for rating eligibility)
    public List<Task> getSignedUpTasksForUser(String userId) {
        ResponseEntity<TaskSignup[]> response = restTemplate.exchange(
                "http://localhost:7777/tasksignup/user/" + userId,
                HttpMethod.GET,
                null,
                TaskSignup[].class);
        TaskSignup[] signups = response.getBody();
        if (signups == null)
            return List.of();

        List<Task> tasks = new ArrayList<>();
        for (TaskSignup signup : signups) {
            String taskId = signup.getTaskId();
            ResponseEntity<Task> taskResp = restTemplate.getForEntity(
                    "http://localhost:7777/task/getTaskById/" + taskId,
                    Task.class);
            if (taskResp.getBody() != null)
                tasks.add(taskResp.getBody());
        }
        return tasks;
    }

}
