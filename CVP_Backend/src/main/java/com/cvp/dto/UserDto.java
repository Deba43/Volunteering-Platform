package com.cvp.dto;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    @NotNull(message = "Provide value for First name")
    private String firstName;

    @NotNull(message = "Provide value for last name")
    private String lastName;

    @NotNull(message = "Provide value for User Name")
    private String userName;

    @NotNull(message = "Provide value for password")
    private String password;

    @NotNull(message = "Provide value for email")
    private String email;

    @NotNull(message = "Provide value for phoneNumber")
    private String phoneNumber;

    @NotNull(message = "Provide value for email")
    private String alternativeEmail;

    @NotNull(message = "Provide value for gender")
    private String gender;

}

