package com.cvp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponseDTO {

    @NotNull(message = "taskName is required")
    private String taskName;

    @NotNull(message = "ratingValue is required")
    private String ratingValue;

    @NotNull(message = "review is required")
    private String review;


}
