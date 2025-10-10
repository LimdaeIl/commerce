package com.friday.commerce.user.infrastructure.jpa;

import com.friday.commerce.user.domain.entity.User;
import com.friday.commerce.user.domain.repository.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<User, Long>, UserRepository {

}
