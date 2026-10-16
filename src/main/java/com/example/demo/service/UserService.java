package com.example.demo.service;

import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.GardenBed;
import com.example.demo.model.User;
import com.example.demo.repository.GardenBedRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class UserService {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]{1,64}@[^@\\s]{1,255}$");

    private final UserRepository users;
    private final GardenBedRepository beds;
    private final GardenBedService bedService;

    public UserService(UserRepository users, GardenBedRepository beds, GardenBedService bedService) {
        this.users = users;
        this.beds = beds;
        this.bedService = bedService;
    }

    public List<User> findAll() {
        return users.findAll();
    }

    public User findById(Long id) {
        return users.findById(id).orElseThrow(() -> new NotFoundException("Пользователь " + id + " не найден"));
    }

    public User create(User data) {
        Validation.text(data.name(), "name", 100);
        validateEmail(data.email());
        return users.save(new User(null, data.name().trim(), data.email().trim()), null);
    }

    public User update(Long id, User patch) {
        User current = findById(id);
        String name = current.name();
        String email = current.email();
        if (patch.name() != null) {
            Validation.text(patch.name(), "name", 100);
            name = patch.name().trim();
        }
        if (patch.email() != null) {
            validateEmail(patch.email());
            email = patch.email().trim();
        }
        return users.save(new User(id, name, email), id);
    }

    /** Удаляет пользователя вместе с его грядками (и их посадками/урожаями) — висячих ссылок не остаётся. */
    public void delete(Long id) {
        findById(id);
        for (GardenBed bed : beds.findByOwnerId(id)) {
            bedService.delete(bed.id());
        }
        users.deleteById(id);
    }

    private void validateEmail(String email) {
        Validation.required(email, "email");
        if (!EMAIL.matcher(email.trim()).matches()) {
            throw new BadRequestException("Некорректный email");
        }
    }
}
