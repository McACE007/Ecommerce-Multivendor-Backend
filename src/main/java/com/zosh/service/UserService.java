package com.zosh.service;

import com.zosh.model.User;
import com.zosh.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    private final JwtService jwtService;

    public User findUserByToken(String token) throws Exception {
        String email = jwtService.getEmailFromToken(token);
        User user = userRepo.findByEmail(email);

        if (user == null)
            throw new Exception("user not found with that email");

        return user;
    }

    public User findUserByEmail(String email) throws Exception {
        User user = userRepo.findByEmail(email);

        if (user == null)
            throw new Exception("user not found with that email");

        return user;
    }
}
