package com.cvp.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.cvp.model.User;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    @Autowired
    DynamoDBMapper dynamoDBMapper;

    public List<User> findAll() {
        return dynamoDBMapper.scan(User.class, new DynamoDBScanExpression());
    }

    public Optional<User> findById(String id) {
        return Optional.ofNullable(dynamoDBMapper.load(User.class, id));
    }

    public Optional<User> findByEmail(String email) {
        List<User> u = dynamoDBMapper.scan(User.class, new DynamoDBScanExpression());
        return u.stream().filter(user -> user.getEmail().equals(email)).findFirst();
    }

    public Optional<User> findByResetToken(String token) {
        List<User> users = dynamoDBMapper.scan(User.class, new DynamoDBScanExpression());
        return users.stream().filter(user -> token.equals(user.getResetToken())).findFirst();
    }

}
