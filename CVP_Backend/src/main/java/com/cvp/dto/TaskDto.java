package com.cvp.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {

    @NotNull(message = "Provide value for title")
    private String title;

    @NotNull(message = "Provide value for description")
    private String description;

    @NotNull(message = "Provide value for location")
    private String location;

    @NotNull(message = "Provide value for status")
    private String status;

    @NotNull(message = "Provide value for priority")
    private String priority;

    @NotNull(message = "Provide value for category")
    private String category;

    @NotNull(message = "Provide value for event date")
    @FutureOrPresent(message = "Event start date should be either current or future date")
    private LocalDate eventDate;

    @NotNull(message = "Provide value for end date")
    @FutureOrPresent(message = "Event end date should be either current or future date")
    private LocalDate endDate;
}

