package com.cvp.service;

import com.cvp.repository.OrganizationRepository;
import com.cvp.repository.TaskRepository;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.cvp.dto.OrganizationDto;
import com.cvp.exception.InvalidEntityException;
import com.cvp.model.Organization;
import com.cvp.model.Task;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class OrganizationService {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationService.class);
    @Autowired
    private final OrganizationRepository organizationRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final DynamoDBMapper dynamoDBMapper;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TaskRepository taskRepository;

    public OrganizationService(OrganizationRepository organizationRepository, PasswordEncoder passwordEncoder,
            DynamoDBMapper dynamoDBMapper) {
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAllOrganization();
    }

    public Organization getOrganizationById(String id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new InvalidEntityException("Organization with ID " + id + " not found."));
    }

    public String registerOrganization(OrganizationDto dto) {
        Optional<Organization> alreadyExists = organizationRepository.findByEmailOrPhoneNumber(
                dto.getEmail(), dto.getPhoneNumber());

        if (alreadyExists.isPresent()) {
            throw new InvalidEntityException("An organization with this email or phone number already exists.");
        }

        Organization organization = new Organization();
        organization.setName(dto.getName());
        organization.setWebsite(dto.getWebsite());
        organization.setLocation(dto.getLocation());
        organization.setEmail(dto.getEmail());
        organization.setPhoneNumber(dto.getPhoneNumber());
        organization.setPassword(passwordEncoder.encode(dto.getPassword()));

        dynamoDBMapper.save(organization);

        String subject = "Welcome to Our Platform!";
        String text = "Dear " + organization.getName() + ",\n\n" +
                "Thank you for registering your organization with us. " +
                "We are excited to have you on board.\n\n" +
                "Best Regards,\n" +
                "Community Volunteering Platform";

        try {
            emailService.sendEmail(organization.getEmail(), subject, text);
        } catch (Exception e) {
            logger.error("Error sending welcome email to {}: {}", organization.getEmail(), e.getMessage());
        }

        return "Organization registration successful!";
    }

    public Organization updateOrganization(@Valid Organization organization) {
        Optional<Organization> existingOrg = organizationRepository.findById(organization.getId());
        if (existingOrg.isPresent()) {
            if (!organization.getPassword().equals(existingOrg.get().getPassword())) {
                organization.setPassword(passwordEncoder.encode(organization.getPassword()));
            }
            dynamoDBMapper.save(organization);
            return organization;
        } else {
            throw new InvalidEntityException("Organization not found.");
        }
    }

    public String deleteOrganization(String id) throws InvalidEntityException {
        Optional<Organization> org = organizationRepository.findById(id);
        if (org == null) {
            throw new InvalidEntityException("Organization with ID " + id + " not found.");
        }
        dynamoDBMapper.delete(org);
        return "Organization deleted successfully!";
    }

    public List<Task> getTasksByOrganizationId(String orgId) {
        List<Task> tasks = taskRepository.findAllByOrgId(orgId);
        if (tasks.isEmpty()) {
            throw new InvalidEntityException("No tasks found for Organization with ID " + orgId);
        }
        return tasks;
    }

    public Organization login(String email, String rawPassword) {
        Organization organization = organizationRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(rawPassword, organization.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        return organization;
    }

    public boolean isOrganizationEmail(String email) {

        List<Organization> orgList = dynamoDBMapper.scan(Organization.class, new DynamoDBScanExpression());

        // Check if any organization matches the provided email
        return orgList.stream()
                .anyMatch(org -> email.equals(org.getEmail())); // Check if email matches
    }

    public Organization getOrganizationByEmail(String email) {
        return organizationRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Organization not found"));
    }

}
