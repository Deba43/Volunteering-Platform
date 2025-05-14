package com.cvp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.cvp.model.Organization;

@Repository
public class OrganizationRepository {

    @Autowired
    private final DynamoDBMapper dynamoDBMapper;

    public OrganizationRepository(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;

    }

    public List<Organization> findAllOrganization() {
        return dynamoDBMapper.scan(Organization.class, new DynamoDBScanExpression());
    }

    public Organization existsById(String id) {
        return dynamoDBMapper.load(Organization.class, id);
    }

    public Optional<Organization> findByEmailOrPhoneNumber(String email, String phoneNumber) {
        List<Organization> organizations = dynamoDBMapper.scan(Organization.class, new DynamoDBScanExpression());
        return organizations.stream()
                .filter(org -> 
                    org.getEmail().trim().equalsIgnoreCase(email.trim()) || 
                    org.getPhoneNumber().trim().equals(phoneNumber.trim()))
                .findFirst();
    }
    

    public Optional<Organization> findById(String id) {
        return Optional.ofNullable(dynamoDBMapper.load(Organization.class, id));
    }

    public Optional<Organization> findByEmail(String email) {
        List<Organization> org = dynamoDBMapper.scan(Organization.class, new DynamoDBScanExpression());
        return org.stream().filter(user -> user.getEmail().equals(email)).findFirst();
    }

}
