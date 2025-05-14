package com.cvp.frontend.model;

import lombok.Data;

@Data
public class Rating {
    private String taskId;
    private String userId;
    private String ratingValue;
    private String review;
}
