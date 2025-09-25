package com.friday.commerce.user.infrastructure.jpa;

import com.friday.commerce.user.domain.entity.User;
import com.friday.commerce.user.domain.repository.JpaUserRepository;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaUserRepositoryImpl extends JpaRepository<User, Long>, JpaUserRepository {

}
