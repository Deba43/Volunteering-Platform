package com.cvp.frontend.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.cvp.frontend.model.Organization;

import java.util.Arrays;
import java.util.List;

@Service
public class OrganizationService {

    private final String BASE_URL = "http://localhost:7777/organization";

    public String registerOrganization(Organization organization) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response =
            restTemplate.postForEntity(BASE_URL + "/register", organization, String.class);
        return response.getBody();
    }

    public List<Organization> getAllOrganizations() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Organization[]> response =
            restTemplate.getForEntity(BASE_URL + "/all", Organization[].class);
        Organization[] organizations = response.getBody();
        return Arrays.asList(organizations);
    }
    
    // Retrieve a single organization by its ID
    public Organization getOrganizationById(String id) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Organization> response =
            restTemplate.getForEntity(BASE_URL + "/" + id, Organization.class);
        return response.getBody();
    }
    
    // Update organization details
    public Organization updateOrganization(Organization organization) {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Organization> requestEntity = new HttpEntity<>(organization);
        ResponseEntity<Organization> response = restTemplate.exchange(
            BASE_URL + "/update",
            HttpMethod.PUT,
            requestEntity,
            Organization.class
        );
        return response.getBody();
    }


    // Delete organization by ID
    public String deleteOrganization(String id) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response =
            restTemplate.exchange(BASE_URL + "/" + id, HttpMethod.DELETE, null, String.class);
        return response.getBody();
    }
}
