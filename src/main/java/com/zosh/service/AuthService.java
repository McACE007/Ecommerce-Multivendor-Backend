package com.zosh.service;

import com.zosh.constants.ExceptionMessages;
import com.zosh.domain.USER_ROLE;
import com.zosh.exception.SellerAlreadyExistsException;
import com.zosh.exception.UserAlreadyExistsException;
import com.zosh.model.*;
import com.zosh.repository.CartRepo;
import com.zosh.repository.SellerRepo;
import com.zosh.repository.UserRepo;
import com.zosh.request.LoginRequest;
import com.zosh.request.SignupRequest;
import com.zosh.response.ApiResponse;
import com.zosh.response.AuthResponse;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.zosh.constants.GlobalConstants.SELLER_PREFIX;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final CartRepo cartRepo;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final UserDetailsService userDetailsService;
    private final SellerService sellerService;
    private final SellerRepo sellerRepo;
    private final VerificationCodeService verificationCodeService;

    public ApiResponse sentOtp(String email) throws MessagingException {
        if (email.startsWith(SELLER_PREFIX))
            email = email.substring(SELLER_PREFIX.length());

        String otp = verificationCodeService.generateOTP(email);

        String subject = "zosh bazaar login/signup otp";
        String text = "your login/signup otp is - ";

        emailService.sendVerificationOtpEmail(email, otp, subject, text);

        return ApiResponse.builder().message("otp send successfully").build();
    }

    public AuthResponse signup(SignupRequest request) throws SellerAlreadyExistsException, UserAlreadyExistsException {
        verificationCodeService.verifyOTP(request.getOtp());

        Authentication authentication = null;
        AuthResponse response = null;
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (request.getEmail().startsWith(SELLER_PREFIX)) {
            String email = request.getEmail().substring(SELLER_PREFIX.length());
            sellerRepo.findByEmail(email).ifPresent(existingSeller -> {
                log.error(ExceptionMessages.SELLER_ALREADY_EXISTS_DEV, existingSeller.getEmail());
                throw new SellerAlreadyExistsException(ExceptionMessages.SELLER_ALREADY_EXISTS_USER);
            });

            Seller newSeller = new Seller();
            newSeller.setEmail(email);
            newSeller.setSellerName(request.getFullName());
            newSeller.setRole(USER_ROLE.ROLE_SELLER);
            newSeller.setMobile("2345653675");
            newSeller.setBusinessDetails(new BusinessDetails());
            newSeller.setBankDetails(new BankDetails());
            newSeller.setPassword(passwordEncoder.encode(request.getOtp()));
            newSeller.setGSTIN("");

            Seller savedSeller = sellerRepo.save(newSeller);

            authorities.add(new SimpleGrantedAuthority(savedSeller.getRole().toString()));
            authentication = new UsernamePasswordAuthenticationToken(savedSeller.getEmail(), null, authorities);

            response = AuthResponse.builder()
                    .token(jwtService.generateToken(authentication))
                    .message("register success")
                    .role(USER_ROLE.ROLE_SELLER)
                    .build();
        } else {
            userRepo.findByEmail(request.getEmail()).ifPresent(existingUser -> {
                log.error(ExceptionMessages.USER_ALREADY_EXISTS_DEV, existingUser.getEmail());
                throw new UserAlreadyExistsException(ExceptionMessages.USER_ALREADY_EXISTS_USER);
            });

            User newUser = new User();
            newUser.setEmail(request.getEmail());
            newUser.setFullName(request.getFullName());
            newUser.setRole(USER_ROLE.ROLE_CUSTOMER);
            newUser.setMobile("2345653675");
            newUser.setPassword(passwordEncoder.encode(request.getOtp()));
            User savedUser = userRepo.save(newUser);

            Cart cart = new Cart();
            cart.setUser(savedUser);
            cartRepo.save(cart);
            authorities.add(new SimpleGrantedAuthority(newUser.getRole().toString()));
            authentication = new UsernamePasswordAuthenticationToken(newUser.getEmail(), null, authorities);
            response = AuthResponse.builder()
                    .token(jwtService.generateToken(authentication))
                    .message("register success")
                    .role(USER_ROLE.ROLE_CUSTOMER)
                    .build();
        }
        return response;
    }

    public AuthResponse login(LoginRequest request) throws Exception {

        Authentication authentication = authenticate(request.getEmail(), request.getOtp());

        SecurityContextHolder.getContext().setAuthentication(authentication);
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String roleName = authorities.isEmpty() ? null : authorities.iterator().next().getAuthority();

        String token = jwtService.generateToken(authentication);

        return AuthResponse.builder()
                .token(token)
                .message("login success")
                .role(USER_ROLE.valueOf(roleName))
                .build();
    }

    private Authentication authenticate(String username, String otp) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (userDetails == null)
            throw new BadCredentialsException("Invalid email");

        verificationCodeService.verifyOTPWithEmail(username, otp);

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
