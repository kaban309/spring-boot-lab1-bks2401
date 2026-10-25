package com.example.demo.service;

import com.example.demo.dto.UserDto;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class UserService {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]{1,64}@[^@\\s]{1,190}$");

    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return users.findAll();
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return users.findById(id).orElseThrow(() -> new NotFoundException("Пользователь " + id + " не найден"));
    }

    @Transactional
    public User create(UserDto.Request r) {
        Validation.text(r.name(), "name", 100);
        String email = validEmail(r.email());
        if (users.existsByEmail(email)) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }
        return users.save(new User(r.name().trim(), email));
    }

    @Transactional
    public User update(Long id, UserDto.Request r) {
        User user = findById(id);
        if (r.name() != null) {
            Validation.text(r.name(), "name", 100);
            user.setName(r.name().trim());
        }
        if (r.email() != null) {
            String email = validEmail(r.email());
            if (!email.equals(user.getEmail()) && users.existsByEmail(email)) {
                throw new ConflictException("Пользователь с таким email уже существует");
            }
            user.setEmail(email);
        }
        return user;
    }

    /** Удаляет пользователя вместе с его грядками, посадками и сборами (каскад). */
    @Transactional
    public void delete(Long id) {
        users.delete(findById(id));
    }

    private String validEmail(String email) {
        Validation.required(email, "email");
        String e = email.trim();
        if (!EMAIL.matcher(e).matches()) {
            throw new BadRequestException("Некорректный email");
        }
        return e;
    }
}
