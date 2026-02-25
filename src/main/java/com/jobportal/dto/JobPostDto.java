package com.jobportal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JobPostDto {

    @NotBlank(message = "Job title is required")
    private String jobTitle;

    @NotBlank(message = "Job description is required")
    private String descriptionOfJob;

    @NotBlank(message = "Job type is required")
    private String jobType;

    private String salary;

    private String remote;

    @NotBlank(message = "Company name is required")
    private String companyName;

    private String companyLogo;

    @NotBlank(message = "City is required")
    private String city;

    private String state;

    @NotBlank(message = "Country is required")
    private String country;
}
