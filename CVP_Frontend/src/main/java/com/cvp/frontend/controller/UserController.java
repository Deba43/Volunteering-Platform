package com.cvp.frontend.controller;

import com.cvp.frontend.model.User;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private RestTemplate restTemplate;

    private final String BASE_URL = "http://localhost:7777";

    // Home Page
    @GetMapping("/")
    public String home() {
        return "homepage";
    }

    // Show Registration Form
    @GetMapping("/registerForm")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "registration";
    }

    // Register User
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        try {
            ResponseEntity<User> response = restTemplate.postForEntity(
                    BASE_URL + "/users/register", user, User.class);
            model.addAttribute("message", "User added successfully: " +
                    response.getBody().getFirstName());
        } catch (HttpClientErrorException e) {
            System.out.println("enterd to error section");
            handleException(e, model);
        }
        return "volunteer-register";
    }

    @GetMapping("/confirm")
    public String showVerify(Model model) {
        model.addAttribute("user", new User());
        return "volunteer-register";
    }

    @PostMapping("/confirm")
    public String VerifyUser(@ModelAttribute User user, Model model) {
        try {
            ResponseEntity<User> response = restTemplate.postForEntity(
                    BASE_URL + "/users/register", user, User.class);
            model.addAttribute("message",
                    "User added successfully: " + response.getBody().getFirstName());
        }
        // catch both 4xx and 5xx
        catch (HttpClientErrorException | HttpServerErrorException e) {
            handleException(e, model);
        }
        // catch everything else too
        catch (RestClientException e) {
            model.addAttribute("message", "Registration service error: " + e.getMessage());
        }
        return "verify_template";
    }

    // View User Profile
    @GetMapping("/profile/{id}")
    public String viewProfile(@PathVariable String id, Model model, HttpSession session) {
        try {
            // Check if already in session (e.g., from login)
            User sessionUser = (User) session.getAttribute("loggedInUser");
            if (sessionUser == null || !sessionUser.getId().equals(id)) {
                // Fetch user via API and store in session
                String apiUrl = BASE_URL + "/users/" + id;
                ResponseEntity<User> response = restTemplate.getForEntity(apiUrl, User.class);
                if (response.getStatusCode() == HttpStatus.OK) {
                    sessionUser = response.getBody();
                    session.setAttribute("loggedInUser", sessionUser);
                } else {
                    model.addAttribute("errorMessage", "User not found");
                    return "user_profile";
                }
            }
            model.addAttribute("user", sessionUser);
        } catch (HttpClientErrorException e) {
            handleException(e, model);
        }
        return "user_profile";
    }

    // Show Edit User Page
    @GetMapping("/edit/{id}")
    public String editUser(@PathVariable String id, Model model) {
        try {
            String apiUrl = BASE_URL + "/users/" + id;
            ResponseEntity<User> response = restTemplate.getForEntity(apiUrl, User.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                model.addAttribute("user", response.getBody());
            } else {
                model.addAttribute("errorMessage", "User not found");
            }
        } catch (HttpClientErrorException e) {
            handleException(e, model);
        }
        return "edit_user";
    }

    // Process Update User
    @PutMapping("/updateUser/{id}")
    public String updateUser(@PathVariable String id, @ModelAttribute User user, Model model) {

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<User> requestEntity = new HttpEntity<>(user, headers);

            ResponseEntity<User> response = restTemplate.exchange(
                    BASE_URL + "/users/edit/" + id,
                    HttpMethod.PUT,
                    requestEntity,
                    User.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                model.addAttribute("newuser", "User Updated Successfully");
            } else {
                model.addAttribute("error", "Update failed.");
            }

        } catch (HttpClientErrorException e) {
            handleException(e, model);
        }

        return "edit_user";
    }

    // Delete User
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable String id, Model model) {
        try {
            restTemplate.delete(BASE_URL + "/users/delete/" + id);
            model.addAttribute("message", "User deleted successfully");
        } catch (HttpClientErrorException e) {
            handleException(e, model);
        }
        return "redirect:/users/";
    }

    // Helper method to handle exceptions
    private void handleException(HttpStatusCodeException e, Model model) {
        try {
            Map<String, String> errors = new ObjectMapper().readValue(
                    e.getResponseBodyAsString(), new TypeReference<Map<String, String>>() {
                    });
            model.addAttribute("errorMessage", errors.get("message"));
        } catch (JsonProcessingException ex) {
            model.addAttribute("errorMessage", "An error occurred while processing the request.");
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Clear session
        return "volunteer-login"; // Redirect to login page
    }
}