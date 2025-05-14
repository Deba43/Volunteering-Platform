package com.cvp.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDto {

    @NotNull(message = "Name can't be null")
    private String name;

    @NotNull(message = "Provide value for website")
    private String website;

    @NotNull(message = "Provide value for location")
    private String location;

    @NotNull(message = "Provide value for email")
    private String email;

    @NotNull(message = "Provide value for phoneNumber")
    private String phoneNumber;

    private List<String> taskIds;

    @NotNull(message = "Provide value for password")
    private String password;

}
