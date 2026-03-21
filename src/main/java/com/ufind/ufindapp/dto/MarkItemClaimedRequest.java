package com.ufind.ufindapp.dto;

import com.ufind.ufindapp.validation.CollegeEmail;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record MarkItemClaimedRequest(
    @NotNull UUID id,
    @NotBlank String claimerName,
    @NotBlank @CollegeEmail String claimerEmail,
    @NotBlank @Size(min = 8, max = 8) String claimerCollegeId
){}