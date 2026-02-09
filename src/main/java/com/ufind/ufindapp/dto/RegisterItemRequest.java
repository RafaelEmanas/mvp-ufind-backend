package com.ufind.ufindapp.dto;

import com.ufind.ufindapp.entity.ItemStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RegisterItemRequest(
    @NotBlank String title,
    @NotBlank String description,
    @NotNull LocalDate dateFound,
    @NotBlank String locationFound,
    ItemStatus status,
    String imageUrl,
    String contactInfo
) {
    
}
