package com.cvp.frontend.service;

import com.cvp.frontend.model.Login;
import com.cvp.frontend.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class LoginService {

    private static final String BACKEND_URL = "http://localhost:7777/users/login";

    @Autowired
    private RestTemplate restTemplate;

    public User login(Login loginRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Login> request = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<User> response = restTemplate.postForEntity(BACKEND_URL, request, User.class);
        return response.getBody();
    }
}
