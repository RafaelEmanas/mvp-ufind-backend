package com.ufind.ufindapp.dto;

import java.util.UUID;

import com.ufind.ufindapp.entity.UserRole;

public record UserInfoDTO(
    UUID id,
    UserRole role
) {}
