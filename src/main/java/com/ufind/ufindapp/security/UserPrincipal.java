package com.ufind.ufindapp.security;

import com.ufind.ufindapp.entity.UserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.UUID;

@Getter
public class UserPrincipal extends User {

    private final UUID id;
    private final UserRole role;

    public UserPrincipal(UUID id, String email, String password, UserRole role, Collection<? extends GrantedAuthority> authorities) {
        super(email, password, authorities);
        this.id = id;
        this.role = role;
    }
}
