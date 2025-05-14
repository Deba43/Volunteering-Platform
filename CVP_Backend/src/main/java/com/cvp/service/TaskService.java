package com.cvp.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.cvp.dto.TaskDto;
import com.cvp.enums.Status;
import com.cvp.exception.InvalidEntityException;
import com.cvp.model.Organization;
import com.cvp.model.Task;
import com.cvp.repository.OrganizationRepository;
import com.cvp.repository.TaskRepository;

import org.springframework.beans.factory.annotation.Autowired;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    OrganizationRepository organizationRepository;
    @Autowired
    EmailService emailService;
    @Autowired
    private final DynamoDBMapper dynamoDBMapper;


    public TaskService(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;

    }

    public List<Task> getAllTasks() {
        return taskRepository.findAllTasks();
    }

    public Task getTaskById(String taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new InvalidEntityException("Task with ID " + taskId + " not found."));
    }

    public List<Task> getTaskByName(String title) {
        List<Task> tasks = taskRepository.findByTitleIgnoreCase(title);
        if (tasks.isEmpty()) {
            throw new InvalidEntityException("No tasks found with title: " + title);
        }
        return tasks;
    }

    public List<Task> getTaskByLocation(String location) {
        List<Task> tasks = taskRepository.findByLocationIgnoreCase(location);
        if (tasks.isEmpty()) {
            throw new InvalidEntityException("No tasks found in location: " + location);
        }
        return tasks;
    }

    public List<Task> getTaskByCategory(String category) {
        List<Task> tasks = taskRepository.findByCategoryIgnoreCase(category);
        if (tasks.isEmpty()) {
            throw new InvalidEntityException("No tasks found in category: " + category);
        }
        return tasks;
    }

    public List<Task> getTaskByDate(LocalDate eventDate) {
        List<Task> tasks = taskRepository.findByEventDate(eventDate);
        if (tasks.isEmpty()) {
            throw new InvalidEntityException("No tasks found on date: " + eventDate);
        }
        return tasks;
    }

    public Task saveTask(String org_id, TaskDto taskDto) {
        if (taskDto.getTitle() == null || taskDto.getTitle().isEmpty()) {
            throw new InvalidEntityException("Title cannot be blank");
        }
        if (taskDto.getDescription() == null || taskDto.getDescription().isEmpty()) {
            throw new InvalidEntityException("Description cannot be blank");
        }
        if (taskDto.getLocation() == null || taskDto.getLocation().isEmpty()) {
            throw new InvalidEntityException("Location cannot be blank");
        }
        if (taskDto.getPriority() == null) {
            throw new InvalidEntityException("Priority must be set");
        }
        if (taskDto.getCategory() == null) {
            throw new InvalidEntityException("Category cannot be blank");
        }
        if (taskDto.getEventDate() == null) {
            throw new InvalidEntityException("Event date is required");
        }

        Organization organization = organizationRepository.findById(org_id)
                .orElseThrow(() -> new InvalidEntityException("Organization with ID " + org_id + " not found"));

        Task task = new Task();
        task.setTitle(taskDto.getTitle());
        task.setDescription(taskDto.getDescription());
        task.setLocation(taskDto.getLocation());
        task.setPriority(taskDto.getPriority());
        task.setCategory(taskDto.getCategory());
        task.setEventDate(taskDto.getEventDate());
        task.setEndDate(taskDto.getEndDate());

        // Convert enum to String or use directly if String is provided in DTO
        if (taskDto.getStatus() == null) {
            task.setStatus(Status.PENDING.name()); // convert to String
        } else {
            task.setStatus(taskDto.getStatus().toUpperCase()); // assuming input is String
        }

        task.setO_id(organization.getId());

        dynamoDBMapper.save(task);
        sendTaskSave(task, organization.getEmail());

        return task;
    }

    public String sendTaskSave(Task task, String orgEmail) {
        String subject = "New Task Created: " + task.getTitle();
        String message = "Your task has been successfully created.\n\n"
                + "Title: " + task.getTitle() + "\n"
                + "Description: " + task.getDescription() + "\n"
                + "Location: " + task.getLocation() + "\n"
                + "Priority: " + task.getPriority() + "\n"
                + "Category: " + task.getCategory() + "\n"
                + "Status: " + task.getStatus() + "\n"
                + "Event Date: " + task.getEventDate() + "\n"
                + "End Date: " + task.getEndDate() + "\n";

        try {
            emailService.sendEmail(orgEmail, subject, message);
            return "Email sent successfully to " + orgEmail;
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to send email to " + orgEmail;
        }
    }

    public List<Task> getAvailableTasks() {
        return taskRepository.findByEventDateGreaterThanEqual(LocalDate.now());
    }

    public List<Task> searchTasks(String title, String location, String category, LocalDate eventDate) {
        List<Task> tasks = taskRepository.findTasksByFilters(title, location, category, eventDate);
        if (tasks.isEmpty()) {
            throw new InvalidEntityException("No tasks match the given search criteria.");
        }
        return tasks;
    }

    public Task updateTask(String taskId, String org_id, Task updatedTask) {
        Task task = getTaskById(taskId);

        if (updatedTask.getTitle() == null || updatedTask.getTitle().isEmpty()) {
            throw new InvalidEntityException("Title cannot be blank");
        }
        if (updatedTask.getDescription() == null || updatedTask.getDescription().isEmpty()) {
            throw new InvalidEntityException("Description cannot be blank");
        }
        if (updatedTask.getLocation() == null || updatedTask.getLocation().isEmpty()) {
            throw new InvalidEntityException("Location cannot be blank");
        }
        if (updatedTask.getPriority() == null) {
            throw new InvalidEntityException("Priority must be set");
        }
        if (updatedTask.getCategory() == null) {
            throw new InvalidEntityException("Category cannot be blank");
        }
        if (updatedTask.getEventDate() == null) {
            throw new InvalidEntityException("Event date is required");
        }

        Organization organization = organizationRepository.findById(org_id)
                .orElseThrow(() -> new InvalidEntityException("Organization with ID " + org_id + " not found"));

        task.setTitle(updatedTask.getTitle());
        task.setDescription(updatedTask.getDescription());
        task.setLocation(updatedTask.getLocation());
        task.setStatus(updatedTask.getStatus());
        task.setPriority(updatedTask.getPriority());
        task.setCategory(updatedTask.getCategory());
        task.setEventDate(updatedTask.getEventDate());
        task.setEndDate(updatedTask.getEndDate());

        task.setO_id(organization.getId());

        dynamoDBMapper.save(task);
        sendUpdateTaskSave(task, organization.getEmail());

        return task;

    }

    public String sendUpdateTaskSave(Task task, String orgEmail) {
        String subject = "New Task Created: " + task.getTitle();
        String message = "Your task has been successfully updated.\n\n"
                + "Title: " + task.getTitle() + "\n"
                + "Description: " + task.getDescription() + "\n"
                + "Location: " + task.getLocation() + "\n"
                + "Priority: " + task.getPriority() + "\n"
                + "Category: " + task.getCategory() + "\n"
                + "Status: " + task.getStatus() + "\n"
                + "Event Date: " + task.getEventDate() + "\n"
                + "End Date: " + task.getEndDate() + "\n";

        try {
            emailService.sendEmail(orgEmail, subject, message);
            return "Email sent successfully to " + orgEmail;
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to send email to " + orgEmail;
        }
    }

}
