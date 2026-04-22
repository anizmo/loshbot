package com.anizmocreations.loshbot.repo;

import com.anizmocreations.loshbot.entity.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID id);
}
