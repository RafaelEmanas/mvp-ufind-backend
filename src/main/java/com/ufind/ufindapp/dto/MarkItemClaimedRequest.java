package com.ufind.ufindapp.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record MarkItemClaimedRequest(
    @NotNull UUID id
){}