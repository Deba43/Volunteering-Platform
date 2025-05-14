package com.cvp.frontend.service;

import com.cvp.frontend.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.Arrays;
import java.util.List;

@Service
public class UserService {

    private final String BACKEND_URL = "http://localhost:7777/users"; // Backend API URL

    @Autowired
    private RestTemplate restTemplate;

    public List<User> getAllUsers() {
        ResponseEntity<User[]> response = restTemplate.getForEntity(BACKEND_URL + "/all", User[].class);
        return Arrays.asList(response.getBody());
    }

    public User getUserById(Long id) {
        return restTemplate.getForObject(BACKEND_URL + "/" + id, User.class);
    }

    public void registerUser(User user) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> request = new HttpEntity<>(user, headers);
        restTemplate.postForEntity(BACKEND_URL + "/add", request, User.class);
    }

    public void updateUser(String id, User user) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> request = new HttpEntity<>(user, headers);
        restTemplate.exchange(BACKEND_URL + "/edit/" + id, HttpMethod.PUT, request, User.class);
    }

    public void deleteUser(String id) {
        restTemplate.delete(BACKEND_URL + "/delete/" + id);
    }
}