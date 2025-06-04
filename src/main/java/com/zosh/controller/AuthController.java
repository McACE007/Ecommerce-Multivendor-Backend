package com.zosh.controller;

import com.zosh.exception.SellerAlreadyExistsException;
import com.zosh.exception.UserAlreadyExistsException;
import com.zosh.request.LoginOtpRequest;
import com.zosh.request.LoginRequest;
import com.zosh.request.SignupRequest;
import com.zosh.response.ApiResponse;
import com.zosh.response.AuthResponse;
import com.zosh.service.AuthService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/request-otp")
    public ResponseEntity<ApiResponse> sendOtp(@RequestBody LoginOtpRequest request) throws MessagingException {
        return ResponseEntity.ok(authService.sentOtp(request.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> sendOtp(@RequestBody LoginRequest request) throws Exception {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request) throws SellerAlreadyExistsException, UserAlreadyExistsException {
        return ResponseEntity.ok(authService.signup(request));
    }
}
