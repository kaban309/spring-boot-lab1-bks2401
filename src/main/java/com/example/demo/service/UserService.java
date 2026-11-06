package com.example.demo.service;

import com.example.demo.dto.UserDto;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.model.UserStatus;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class UserService {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]{1,64}@[^@\\s]{1,190}$");

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return users.findAll();
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return users.findById(id).orElseThrow(() -> new NotFoundException("Пользователь " + id + " не найден"));
    }

    /**
     * Саморегистрация. Всегда создаёт активного садовода: повысить привилегии при регистрации нельзя,
     * роль и статус из запроса не читаются вообще.
     */
    @Transactional
    public User register(UserDto.RegisterRequest r) {
        return createUser(r.name(), r.email(), r.password(), Role.GARDENER, UserStatus.ACTIVE);
    }

    /** Создание пользователя администратором (роль и статус можно задать). */
    @Transactional
    public User create(UserDto.Request r) {
        Role role = r.role() != null ? r.role() : Role.GARDENER;
        UserStatus status = r.status() != null ? r.status() : UserStatus.ACTIVE;
        return createUser(r.name(), r.email(), r.password(), role, status);
    }

    @Transactional
    public User update(Long id, UserDto.Request r) {
        User user = findById(id);
        if (r.name() != null) {
            Validation.text(r.name(), "name", 100);
            user.setName(r.name().trim());
        }
        if (r.email() != null) {
            String email = normalizeEmail(r.email());
            if (!email.equals(user.getEmail()) && users.existsByEmail(email)) {
                throw new ConflictException("Пользователь с таким email уже существует");
            }
            user.setEmail(email);
        }
        if (r.password() != null) {
            PasswordPolicy.validate(r.password(), user.getEmail());
            user.setPasswordHash(passwordEncoder.encode(r.password()));
        }
        Role newRole = r.role() != null ? r.role() : user.getRole();
        UserStatus newStatus = r.status() != null ? r.status() : user.getStatus();
        boolean wasActiveAdmin = isActiveAdmin(user);
        boolean willBeActiveAdmin = newRole == Role.ADMIN && newStatus == UserStatus.ACTIVE;
        if (wasActiveAdmin && !willBeActiveAdmin) {
            requireAnotherActiveAdmin();
        }
        user.setRole(newRole);
        user.setStatus(newStatus);
        return user;
    }

    /** Удаляет пользователя вместе с его грядками, посадками и сборами (каскад). */
    @Transactional
    public void delete(Long id) {
        User user = findById(id);
        if (isActiveAdmin(user)) {
            requireAnotherActiveAdmin();
        }
        users.delete(user);
    }

    private User createUser(String name, String email, String password, Role role, UserStatus status) {
        Validation.text(name, "name", 100);
        String normalized = normalizeEmail(email);
        PasswordPolicy.validate(password, normalized);
        if (users.existsByEmail(normalized)) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }
        return users.save(new User(name.trim(), normalized, passwordEncoder.encode(password), role, status));
    }

    private String normalizeEmail(String email) {
        Validation.required(email, "email");
        String e = email.trim().toLowerCase(Locale.ROOT);
        if (!EMAIL.matcher(e).matches()) {
            throw new BadRequestException("Некорректный email");
        }
        return e;
    }

    private boolean isActiveAdmin(User u) {
        return u.getRole() == Role.ADMIN && u.getStatus() == UserStatus.ACTIVE;
    }

    /** Бизнес-ограничение: в системе всегда должен остаться хотя бы один активный администратор. */
    private void requireAnotherActiveAdmin() {
        if (users.countByRoleAndStatus(Role.ADMIN, UserStatus.ACTIVE) <= 1) {
            throw new ConflictException("Нельзя удалить, заблокировать или понизить последнего активного администратора");
        }
    }
}
