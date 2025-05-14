package com.cvp.service;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.cvp.dto.UserDto;
import com.cvp.exception.InvalidEntityException;
import com.cvp.model.User;
import com.cvp.repository.UserRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    public User saveUser(UserDto userDto) {
        if (userDto.getFirstName() == null || userDto.getFirstName().isEmpty()) {
            throw new InvalidEntityException("First Name cannot be blank");
        }
        if (userDto.getLastName() == null || userDto.getLastName().isEmpty()) {
            throw new InvalidEntityException("Last Name cannot be blank");
        }
        if (userDto.getUserName() == null || userDto.getUserName().isEmpty()) {
            throw new InvalidEntityException("User Name cannot be blank");
        }
        if (userDto.getPassword() == null) {
            throw new InvalidEntityException("Password must be set");
        }
        if (userDto.getEmail() == null) {
            throw new InvalidEntityException("Email cannot be blank");
        }
        if (userDto.getPhoneNumber() == null) {
            throw new InvalidEntityException("Phone Number is required");
        }
        if (userDto.getAlternativeEmail() == null) {
            throw new InvalidEntityException("Alternative Email is required");
        }
        if (userDto.getGender() == null) {
            throw new InvalidEntityException("Gender is necessary");
        }

        User user = new User();
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setUserName(userDto.getUserName());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setEmail(userDto.getEmail());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setAlternativeEmail(userDto.getAlternativeEmail());
        user.setGender(userDto.getGender());

        dynamoDBMapper.save(user);
        return user;
    }

    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            throw new InvalidEntityException("No users found.");
        }
        return users;
    }

    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new InvalidEntityException("User with ID " + id + " not found."));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateUser(String id, User userDetails) {
        User user = getUserById(id);

        if (userDetails.getFirstName() == null || userDetails.getFirstName().isEmpty()) {
            throw new InvalidEntityException("First Name cannot be blank");
        }
        if (userDetails.getLastName() == null || userDetails.getLastName().isEmpty()) {
            throw new InvalidEntityException("Last Name cannot be blank");
        }
        if (userDetails.getUserName() == null || userDetails.getUserName().isEmpty()) {
            throw new InvalidEntityException("User Name cannot be blank");
        }
        if (userDetails.getPassword() == null) {
            throw new InvalidEntityException("Password must be set");
        }
        if (userDetails.getEmail() == null) {
            throw new InvalidEntityException("Email cannot be blank");
        }
        if (userDetails.getPhoneNumber() == null) {
            throw new InvalidEntityException("Phone Number is required");
        }
        if (userDetails.getAlternativeEmail() == null) {
            throw new InvalidEntityException("Alternative Email is required");
        }
        if (userDetails.getGender() == null) {
            throw new InvalidEntityException("Gender is necessary");
        }

        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setUserName(userDetails.getUserName());
        user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        user.setEmail(userDetails.getEmail());
        user.setPhoneNumber(userDetails.getPhoneNumber());
        user.setAlternativeEmail(userDetails.getAlternativeEmail());
        user.setGender(userDetails.getGender());

        dynamoDBMapper.save(user);
        return user;
    }

    public void deleteUser(String id) {
        User user = getUserById(id);
        if (user == null) {
            throw new InvalidEntityException("User with ID " + id + " does not exist.");
        }
        dynamoDBMapper.delete(user);
    }

    public boolean checkOldPassword(String userId, String oldPassword) {
        User user = getUserById(userId);
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }

    public boolean isUserEmail(String email) {

        List<User> user = dynamoDBMapper.scan(User.class, new DynamoDBScanExpression());

        // Check if any organization matches the provided email
        return user.stream()
                .anyMatch(use -> email.equals(use.getEmail())); // Check if email matches
    }

}
