package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository extends InMemoryRepository<User> {
    @Override
    protected User withId(User e, Long id) {
        return new User(id, e.name(), e.email());
    }
}
