package com.cvp.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.cvp.model.Rating;

@Repository
public class RatingRepository {

    @Autowired
    DynamoDBMapper dynamoDBMapper;

    public boolean existsByUserIdAndTaskId(String userId, String taskId) {
        List<Rating> ratings = dynamoDBMapper.scan(Rating.class, new DynamoDBScanExpression());
        return ratings.stream()
                .anyMatch(r -> r.getUserId().equals(userId) && r.getTaskId().equals(taskId));
    }

    public List<Rating> findAll() {
        return dynamoDBMapper.scan(Rating.class, new DynamoDBScanExpression());
    }

    public List<String> findRatedTaskIdsByUserId(String userId) {
        return dynamoDBMapper.scan(Rating.class, new DynamoDBScanExpression())
                .stream()
                .filter(rating -> rating.getUserId().equals(userId))
                .map(Rating::getTaskId)
                .distinct()
                .toList();
    }

    public List<Rating> findByTaskId(String taskId) {
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        List<Rating> allRatings = dynamoDBMapper.scan(Rating.class, scanExpression);

        return allRatings.stream()
                .filter(rating -> taskId.equals(rating.getTaskId()))
                .toList();
    }

}