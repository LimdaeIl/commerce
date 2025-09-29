package com.friday.commerce.user.presentation;

import com.friday.commerce.user.application.usecase.AuthUsecase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class UserController {

    private final AuthUsecase authUsecase;



}
