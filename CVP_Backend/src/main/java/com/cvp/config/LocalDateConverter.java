package com.cvp.config;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import java.time.LocalDate;

public class LocalDateConverter implements DynamoDBTypeConverter<String, LocalDate> {

    @Override
    public String convert(LocalDate date) {
        return date != null ? date.toString() : null; // ISO-8601 format
    }

    @Override
    public LocalDate unconvert(String dateString) {
        return dateString != null ? LocalDate.parse(dateString) : null;
    }
}
