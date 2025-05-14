package com.cvp.service;

import com.cvp.repository.TaskRepository;
import com.cvp.repository.TaskSignupRepository;
import com.cvp.repository.UserRepository;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.cvp.exception.InvalidEntityException;
import com.cvp.model.Task;
import com.cvp.model.TaskSignup;
import com.cvp.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class TaskSignupService {

    @Autowired
    private final TaskSignupRepository taskSignupRepository;

    @Autowired
    private EmailService mailSender;

    @Autowired
    private final DynamoDBMapper dynamoDBMappper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    UserRepository userRepository;

    public TaskSignupService(TaskSignupRepository taskSignupRepository, DynamoDBMapper dynamoDBMapper) {
        this.taskSignupRepository = taskSignupRepository;
        this.dynamoDBMappper = dynamoDBMapper;

    }

    public List<TaskSignup> getAllSignups() {
        return taskSignupRepository.findAll();
    }

    public String registerForTask(TaskSignup taskSignup) {
        if (taskSignup.getSignupDate() == null) {
            taskSignup.setSignupDate(LocalDate.now());
        }

        // 1. Validate User
        User user = userRepository.findById(taskSignup.getUserId()).orElse(null);
        if (user == null) {
            return "User not found";
        }

        // 2. Validate Task
        Task task = taskRepository.findByTitleIgnoreCase(taskSignup.getTaskName())
                .stream().findFirst().orElse(null);
        if (task == null) {
            return "Task not found. Please check the task name.";
        }

        // 3. Set task info
        taskSignup.setTaskName(task.getTitle());
        taskSignup.setTaskId(task.getId());

        // 4. Set validated userId
        taskSignup.setUserId(user.getId());

        // 5. Prevent duplicate registrations
        boolean alreadyRegistered = taskSignupRepository.existsByVolunteerNameAndTaskNameAndSignupDate(
                taskSignup.getVolunteerName(), taskSignup.getTaskName(), taskSignup.getSignupDate());

        if (alreadyRegistered) {
            return "You have already registered for this task on the selected date.";
        }

        // 6. Save to DynamoDB
        dynamoDBMappper.save(taskSignup);

        // 7. Send confirmation email
        mailSender.sendEmailForTaskSignUp(
                user.getEmail(),
                taskSignup.getTaskName(),
                taskSignup.getVolunteerName(),
                taskSignup.getSignupDate());

        return "Registration Successful!";
    }

    public List<TaskSignup> getSignedUpTasksByUserId(String userId) {
        List<TaskSignup> tasks = taskSignupRepository.findTasksByUserId(userId);
        if (tasks.isEmpty()) {
            throw new InvalidEntityException("No task signup found for User ID " + userId);
        }
        return tasks;
    }

    public List<TaskSignup> getTasksByUserId(String userId) {
        return taskSignupRepository.findTasksByUserId(userId);
    }

    public List<TaskSignup> getVolunteersByOrganization(String userId) {
        List<TaskSignup> tasksignup = taskSignupRepository.findVolunteersByOrganization(userId);
        if (tasksignup.isEmpty()) {
            throw new InvalidEntityException("No tasks found for Organization with ID " + userId);
        }
        return tasksignup;
    }

}
