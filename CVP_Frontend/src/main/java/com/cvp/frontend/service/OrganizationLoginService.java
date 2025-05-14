package com.cvp.frontend.service;

import com.cvp.frontend.model.Organization;
import com.cvp.frontend.model.OrganizationLogin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OrganizationLoginService {

    private static final String BACKEND_URL = "http://localhost:7777/organization/login";

    @Autowired
    private RestTemplate restTemplate;

    public Organization login(OrganizationLogin loginRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<OrganizationLogin> request = new HttpEntity<>(loginRequest, headers);

        ResponseEntity<Organization> response = restTemplate.postForEntity(BACKEND_URL, request, Organization.class);
        return response.getBody();
    }
}
