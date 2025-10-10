package com.friday.commerce.user.domain.repository;

import com.friday.commerce.user.domain.entity.User;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    User save(User user);

    Optional<User> findById(Long userId);

    void flush();
}
