package com.ufind.ufindapp.dto;

import com.ufind.ufindapp.entity.UserRole;

import java.util.UUID;

public record UserMeDTO(UUID id, UserRole role) {
}
