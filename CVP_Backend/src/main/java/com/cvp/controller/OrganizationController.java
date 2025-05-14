package com.cvp.controller;

import com.cvp.dto.OrganizationDto;
import com.cvp.exception.InvalidEntityException;
import com.cvp.model.Login;
import com.cvp.model.Organization;
import com.cvp.model.Task;
import com.cvp.model.TaskSignup;
import com.cvp.service.CognitoService;
import com.cvp.service.OrganizationService;
import com.cvp.service.TaskSignupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/organization")
@CrossOrigin(origins = "*")
public class OrganizationController {

    private final OrganizationService organizationService;

    private final TaskSignupService taskSignupService;

    private final CognitoService cognitoService;

    public OrganizationController(OrganizationService organizationService,
            TaskSignupService taskSignupService,
            CognitoService cognitoService) {
        this.organizationService = organizationService;
        this.taskSignupService = taskSignupService;
        this.cognitoService = cognitoService;

    }

    @GetMapping("/all")
    public ResponseEntity<List<Organization>> getAllOrganizations() {
        return ResponseEntity.ok(organizationService.getAllOrganizations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Organization> getOrganization(@PathVariable String id) throws InvalidEntityException {
        return ResponseEntity.ok(organizationService.getOrganizationById(id));
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerOrganization(@RequestBody OrganizationDto dto) {
        cognitoService.signUpOrganization(dto.getEmail(), dto.getPassword());

        organizationService.registerOrganization(dto);

        return ResponseEntity.ok("Organization registered! Check email for verification.");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginOrganization(@RequestBody Login login) {
        // Check if email exists
        if (!organizationService.isOrganizationEmail(login.getEmail())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Email not found."));
        }

        // Authenticate with Cognito and get token
        String token = cognitoService.login(login.getEmail(), login.getPassword());

        // Fetch user from database using EMAIL (to get the ID)
        Organization org = organizationService.getOrganizationByEmail(login.getEmail());

        // Return token + database user ID (NOT email or Cognito ID)
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("orgId", org.getId().toString()); // Return the database-generated ID
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateOrganization(@Valid @RequestBody Organization organization, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getFieldErrors().get(0).getDefaultMessage());
        }
        Organization updated = organizationService.updateOrganization(organization);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteOrganization(@PathVariable String id) throws InvalidEntityException {
        return ResponseEntity.ok(organizationService.deleteOrganization(id));
    }

    @GetMapping("/{org_id}/tasks")
    public ResponseEntity<List<Task>> getTasksByOrganization(@PathVariable String org_id) {
        List<Task> tasks = organizationService.getTasksByOrganizationId(org_id);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{userId}/volunteers")
    public ResponseEntity<List<TaskSignup>> getVolunteersByOrganization(@PathVariable String userId) {
        List<TaskSignup> volunteers = taskSignupService.getVolunteersByOrganization(userId);
        if (volunteers.isEmpty()) {
            throw new InvalidEntityException("No Volunteers found for this Organization : " + userId);
        }
        return ResponseEntity.ok(volunteers);
    }

    // @PostMapping("/login")
    // public ResponseEntity<?> loginOrganization(@Valid @RequestBody
    // OrganizationLoginDto loginDto,
    // BindingResult result) {
    // if (result.hasErrors()) {
    // return
    // ResponseEntity.badRequest().body(result.getFieldErrors().get(0).getDefaultMessage());
    // }
    // try {
    // Organization organization = organizationService.login(loginDto.getEmail(),
    // loginDto.getPassword());
    // return ResponseEntity.ok(organization);
    // } catch (RuntimeException e) {
    // return ResponseEntity.status(401).body(e.getMessage());
    // }
    // }
}
