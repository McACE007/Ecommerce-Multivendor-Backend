package com.zosh.service;

import com.zosh.constants.ExceptionMessages;
import com.zosh.exception.UserNotFoundException;
import com.zosh.model.User;
import com.zosh.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    private final JwtService jwtService;

    public User findUserByToken(String token) throws UserNotFoundException {
        String email = jwtService.getEmailFromToken(token);
        return userRepo.findByEmail(email).orElseThrow(() -> {
            log.error(ExceptionMessages.USER_NOT_FOUND_EMAIL_DEV, email);
            return new UserNotFoundException(ExceptionMessages.USER_NOT_FOUND_USER);
        });
    }

    public User findUserByEmail(String email) throws UserNotFoundException {
        return userRepo.findByEmail(email).orElseThrow(() -> {
            log.error(ExceptionMessages.USER_NOT_FOUND_EMAIL_DEV, email);
            return new UserNotFoundException(ExceptionMessages.USER_NOT_FOUND_USER);
        });
    }
}
