package com.example.demo.security;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.model.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/** Данные пользователя для Spring Security. Состояние учётной записи (BLOCKED) учитывается при входе. */
public class AppUserDetails implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final Role role;
    private final boolean active;

    public AppUserDetails(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.role = user.getRole();
        this.active = user.getStatus() == UserStatus.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /** Заблокированная учётная запись считается заблокированной: войти нельзя. */
    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
