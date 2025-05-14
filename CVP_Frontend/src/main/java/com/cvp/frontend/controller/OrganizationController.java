package com.cvp.frontend.controller;

import com.cvp.frontend.model.Organization;
import com.cvp.frontend.model.Task;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@Controller
@RequestMapping("/organization")
public class OrganizationController {

    private final String BASE_URL = "http://localhost:7777/organization";
    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/form")
    public String addOrganizationForm(Model model) {
        model.addAttribute("organizationModel", new Organization()); // ✅ Match Thymeleaf variable name
        return "add-organization";
    }

    @PostMapping("/register")
    public String registerOrganization(
            @ModelAttribute("organizationModel") Organization organization,
            Model model,
            HttpSession session) {

        System.out.println("Sending Organization to Backend: " + organization);

        try {
            String apiUrl = BASE_URL + "/register";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Organization> requestEntity = new HttpEntity<>(organization, headers);

            ResponseEntity<Organization> response = restTemplate.postForEntity(apiUrl, requestEntity,
                    Organization.class);
            System.out.println("Backend Response: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Store organization in session
                session.setAttribute("loggedInOrg", response.getBody());

                model.addAttribute("message", "Organization registered successfully!");

                // Redirect to organization profile with ID
                return "redirect:/organization/profile/" + response.getBody().getId();
            } else {
                model.addAttribute("message", "Failed to register organization.");
                return "add-organization";
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            model.addAttribute("message", "Error occurred while registering organization.");
            return "add-organization";
        }
    }

    @PostMapping("/confirm")
    public String VerifyUser(@ModelAttribute Organization org, Model model) {
        try {
            ResponseEntity<Organization> response = restTemplate.postForEntity(
                    BASE_URL + "/register", org, Organization.class);
            model.addAttribute("message",
                    "Organization added successfully: " + response.getBody().getName());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            handleException(e, model);
        } catch (RestClientException e) {
            model.addAttribute("message", "Registration service error: " + e.getMessage());
        }
        return "verify_template_org";
    }

    // 👇 Add this method inside the same controller
    private void handleException(HttpStatusCodeException e, Model model) {
        String errorMessage = "Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString();
        model.addAttribute("message", errorMessage);
    }

    @GetMapping("/view")
    public String viewOrganizations(Model model) {
        ResponseEntity<Organization[]> response = restTemplate.getForEntity(BASE_URL + "/all", Organization[].class);
        model.addAttribute("organizations", response.getBody());
        return "view-organization";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable String id, Model model) {
        ResponseEntity<Organization> response = restTemplate.getForEntity(BASE_URL + "/" + id, Organization.class);
        model.addAttribute("organization", response.getBody()); // 🔁 FIXED
        return "edit-organization";
    }

    @PostMapping("/update")
    public String updateOrganization(@ModelAttribute Organization organization,
            BindingResult result,
            Model model,
            HttpSession session) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Organization> requestEntity = new HttpEntity<>(organization, headers);
            ResponseEntity<Organization> response = restTemplate.exchange(
                    BASE_URL + "/update", HttpMethod.PUT, requestEntity, Organization.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                // ✅ Update session attribute
                session.setAttribute("loggedInOrganization", response.getBody());
                System.out.println("Updating org: " + organization.getId() + " - " + organization.getName());

                model.addAttribute("updatedorg", "Organization Updated Successfully");
                return "redirect:/organization/profile/" + organization.getId();
            } else {
                model.addAttribute("error", "Update failed.");
            }

        } catch (HttpClientErrorException e) {
            handleException(e, model);
        }

        return "edit-organization";
    }

    @GetMapping("/delete/{id}")
    public String deleteOrganization(@PathVariable String id, Model model) {
        try {
            restTemplate.delete(BASE_URL + "/" + id);
            model.addAttribute("message", "Organization deleted successfully!");
        } catch (HttpClientErrorException e) {
            model.addAttribute("message", "Failed to delete organization: " + e.getMessage());
        }
        return "index";
    }

    @GetMapping("/{id}/tasks")
    public String getTasksByOrganization(@PathVariable String id, Model model) {
        try {
            model.addAttribute("org_id", id);
            String apiUrl = BASE_URL + "/" + id + "/tasks"; // Backend API
            ResponseEntity<Task[]> response = restTemplate.getForEntity(apiUrl, Task[].class);
            Task[] tasks = response.getBody();

            if (tasks == null || tasks.length == 0) {
                model.addAttribute("errorMessage", "No tasks available for this organization.");
                return "errorOrgTaskNot"; // Redirect to error page if no tasks are found
            } else {
                model.addAttribute("tasks", Arrays.asList(tasks));
            }
        } catch (HttpClientErrorException.NotFound e) { // Handling 404 errors
            model.addAttribute("errorMessage", "No tasks found for the given organization.");
            return "errorOrgTaskNot";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Something went wrong. Please try again later.");
            return "errorOrgTaskNot";
        }

        return "getTaskByOrganization"; // Redirects to Thymeleaf task page
    }

    @GetMapping("/profile/{id}")
    public String viewProfile(@PathVariable String id, Model model, HttpSession session) {
        try {
            String apiUrl = BASE_URL + "/" + id;
            ResponseEntity<Organization> response = restTemplate.getForEntity(apiUrl, Organization.class);

            // Log the response
            System.out.println("API Response Status: " + response.getStatusCode());
            System.out.println("API Response Body: " + response.getBody());

            if (response.getStatusCode() == HttpStatus.OK) {
                Organization org = response.getBody();
                model.addAttribute("org", org);

                // ✅ Set the session attribute if not already set
                if (session.getAttribute("loggedInOrganization") == null) {
                    session.setAttribute("loggedInOrganization", org);
                }
            } else {
                model.addAttribute("errorMessage", "Organization not found");
            }
        } catch (HttpClientErrorException e) {
            handleException(e, model);
        }
        return "organizerProfile";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Clear session
        return "redirect:/organizer"; // Redirect to login page
    }
}
