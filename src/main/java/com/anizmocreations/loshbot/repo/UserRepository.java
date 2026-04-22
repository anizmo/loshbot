package com.anizmocreations.loshbot.repo;

import com.anizmocreations.loshbot.entity.User;
import com.anizmocreations.loshbot.entity.UserRole;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID id);
    List<User> findByRole(UserRole role);
}
