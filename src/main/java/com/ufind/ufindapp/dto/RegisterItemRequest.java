package com.ufind.ufindapp.dto;

import com.ufind.ufindapp.entity.ItemStatus;
import com.ufind.ufindapp.validation.CollegeEmail;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterItemRequest(
    @NotBlank String title,
    @NotBlank String description,
    @NotNull LocalDate dateFound,
    @NotBlank String locationFound,
    ItemStatus status,
    @NotBlank String imageUrl,
    @NotBlank String finderName,
    @NotBlank @CollegeEmail String finderEmail,
    @NotBlank @Size(min = 8, max = 8) String finderCollegeId
) {}
