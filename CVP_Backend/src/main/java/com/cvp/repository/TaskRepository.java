package com.cvp.repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.cvp.model.Task;

@Repository
public class TaskRepository {

        @Autowired
        private final DynamoDBMapper dynamoDBMapper;

        public TaskRepository(DynamoDBMapper dynamoDBMapper) {
                this.dynamoDBMapper = dynamoDBMapper;

        }

        public List<Task> findAllByOrgId(String orgId) {
                Map<String, AttributeValue> eav = new HashMap<>();
                eav.put(":val1", new AttributeValue().withS(orgId));

                DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                                .withFilterExpression("o_id = :val1")
                                .withExpressionAttributeValues(eav);
                return dynamoDBMapper.scan(Task.class, scanExpression);
        }

        public List<Task> findAllTasks() {
                return dynamoDBMapper.scan(Task.class, new DynamoDBScanExpression());
        }

        public Optional<Task> findById(String id) {
                return Optional.ofNullable(dynamoDBMapper.load(Task.class, id));
        }

        public List<Task> findByTitleIgnoreCase(String title) {
                List<Task> tasks = dynamoDBMapper.scan(Task.class, new DynamoDBScanExpression());
                return tasks.stream()
                                .filter(task -> task.getTitle() != null && task.getTitle().equalsIgnoreCase(title))
                                .collect(Collectors.toList());
        }

        public List<Task> findByLocationIgnoreCase(String location) {
                List<Task> tasks = dynamoDBMapper.scan(Task.class, new DynamoDBScanExpression());
                return tasks.stream()
                                .filter(task -> task.getLocation() != null
                                                && task.getLocation().equalsIgnoreCase(location))
                                .collect(Collectors.toList());
        }

        public List<Task> findByCategoryIgnoreCase(String category) {
                List<Task> tasks = dynamoDBMapper.scan(Task.class, new DynamoDBScanExpression());
                return tasks.stream()
                                .filter(task -> task.getCategory() != null
                                                && task.getCategory().equalsIgnoreCase(category))
                                .collect(Collectors.toList());
        }

        public List<Task> findByEventDate(LocalDate eventDate) {
                List<Task> task = dynamoDBMapper.scan(Task.class, new DynamoDBScanExpression());
                return task.stream()
                                .filter(user -> user.getEventDate() != null && user.getEventDate().equals(eventDate))
                                .collect(Collectors.toList());
        }

        public List<Task> findByEventDateGreaterThanEqual(LocalDate eventDate) {
                List<Task> task = dynamoDBMapper.scan(Task.class, new DynamoDBScanExpression());
                return task.stream()
                                .filter(user -> user.getEventDate() != null && !user.getEventDate().isBefore(eventDate))
                                .collect(Collectors.toList());
        }

        public List<Task> findTasksByFilters(String title, String location, String category, LocalDate eventDate) {
                List<Task> taskList = dynamoDBMapper.scan(Task.class, new DynamoDBScanExpression());

                return taskList.stream()
                                .filter(task -> (title == null || task.getTitle().equalsIgnoreCase(title)) &&
                                                (location == null || task.getLocation().equalsIgnoreCase(location)) &&
                                                (category == null || task.getCategory().equalsIgnoreCase(category)) &&
                                                (eventDate == null || eventDate.equals(task.getEventDate())))
                                .collect(Collectors.toList());
        }

}
