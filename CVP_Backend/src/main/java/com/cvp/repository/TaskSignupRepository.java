package com.cvp.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.cvp.model.Task;
import com.cvp.model.TaskSignup;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class TaskSignupRepository {

    @Autowired
    private final DynamoDBMapper dynamoDBMapper;

    public TaskSignupRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;

    }

    public List<TaskSignup> findAll() {
        return dynamoDBMapper.scan(TaskSignup.class, new DynamoDBScanExpression());
    }

    public List<TaskSignup> findVolunteersByOrganization(String userId) {
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":val1", new AttributeValue().withS(userId));

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("userId = :val1")
                .withExpressionAttributeValues(eav);
        return dynamoDBMapper.scan(TaskSignup.class, scanExpression);
    }

    public boolean existsByVolunteerNameAndTaskNameAndSignupDate(String volunteerName, String taskName,
            LocalDate signupDate) {
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("volunteerName = :volName and taskName = :taskName and signupDate = :signupDate")
                .withExpressionAttributeValues(Map.of(
                        ":volName", new AttributeValue().withS(volunteerName),
                        ":taskName", new AttributeValue().withS(taskName),
                        ":signupDate", new AttributeValue().withS(signupDate.toString())));

        List<TaskSignup> result = dynamoDBMapper.scan(TaskSignup.class, scanExpression);
        return !result.isEmpty();
    }

    public List<Task> findAllByOrgId(String orgId) {
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":val1", new AttributeValue().withS(orgId));

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("o_id = :val1")
                .withExpressionAttributeValues(eav);
        return dynamoDBMapper.scan(Task.class, scanExpression);
    }

    public List<TaskSignup> findTasksByUserId(String userId) {
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":val1", new AttributeValue().withS(userId));

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("userId = :val1")
                .withExpressionAttributeValues(eav);
        return dynamoDBMapper.scan(TaskSignup.class, scanExpression);
    }

    // List<TaskSignup> findByVolunteerName(String volunteerName);

    // @Query("SELECT ts.task FROM TaskSignup ts WHERE ts.user.id = :userId AND
    // ts.task.status = 'COMPLETED' AND ts.task.id NOT IN :ratedTaskIds")
    // List<Task> findCompletedTasksNotRatedByUser(@Param("userId") Long userId,
    // @Param("ratedTaskIds") List<Long> ratedTaskIds);

}
