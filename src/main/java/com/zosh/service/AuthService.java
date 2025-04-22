package com.zosh.service;

import com.zosh.domain.USER_ROLE;
import com.zosh.model.*;
import com.zosh.repository.CartRepo;
import com.zosh.repository.SellerRepo;
import com.zosh.repository.UserRepo;
import com.zosh.repository.VerificationCodeRepo;
import com.zosh.request.LoginRequest;
import com.zosh.request.SignupRequest;
import com.zosh.response.ApiResponse;
import com.zosh.response.AuthResponse;
import com.zosh.utils.OtpUtil;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final CartRepo cartRepo;
    private final JwtService jwtService;
    private final VerificationCodeRepo verificationCodeRepo;
    private final EmailService emailService;
    private final UserDetailsService userDetailsService;
    private final SellerService sellerService;
    private final SellerRepo sellerRepo;
    private static final String SELLER_PREFIX = "seller_";

    public ApiResponse sentOtp(String email) throws Exception {
        VerificationCode verificationCode = verificationCodeRepo.findByEmail(email);

        if (verificationCode != null) {
            verificationCodeRepo.delete(verificationCode);
        }

        String otp = OtpUtil.generateOtp();

        verificationCode = new VerificationCode();
        verificationCode.setEmail(email);
        verificationCode.setOtp(otp);
        verificationCodeRepo.save(verificationCode);

        String subject = "zosh bazaar login/signup otp";
        String text = "your login/signup otp is - ";

        emailService.sendVerificationOtpEmail(email.startsWith(SELLER_PREFIX) ? email.substring(SELLER_PREFIX.length()) : email, otp, subject, text);

        ApiResponse response = new ApiResponse();
        response.setMessage("otp send successfully");

        return response;
    }

    public AuthResponse signup(SignupRequest request) throws Exception {
        VerificationCode verificationCode = verificationCodeRepo.findByEmail(request.getEmail());

        if (verificationCode == null || !verificationCode.getOtp().equals(request.getOtp()))
            throw new Exception("wrong otp...");

        Authentication authentication = null;
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (request.getEmail().startsWith(SELLER_PREFIX)) {
            Seller seller = sellerRepo.findByEmail(request.getEmail());
            if (seller != null)
                throw new Exception("Seller already exists");

            Seller newSeller = new Seller();
            newSeller.setEmail(request.getEmail());
            newSeller.setSellerName(request.getFullName());
            newSeller.setRole(USER_ROLE.ROLE_SELLER);
            newSeller.setMobile("2345653675");
            newSeller.setBusinessDetails(new BusinessDetails());
            newSeller.setBankDetails(new BankDetails());
            newSeller.setPassword(passwordEncoder.encode(request.getOtp()));
            newSeller.setGSTIN("");

            Seller savedSeller = sellerRepo.save(newSeller);

            authorities.add(new SimpleGrantedAuthority(newSeller.getRole().toString()));
            authentication = new UsernamePasswordAuthenticationToken(savedSeller.getEmail(), null, authorities);
        } else {
            User user = userRepo.findByEmail(request.getEmail());

            if (user != null)
                throw new Exception("User already exists");

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
        }

        String token = jwtService.generateToken(authentication);

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setMessage("register success");
        response.setRole(USER_ROLE.ROLE_CUSTOMER);

        return response;
    }

    public AuthResponse login(LoginRequest request) throws Exception {

        Authentication authentication = authenticate(request.getEmail(), request.getOtp());

        SecurityContextHolder.getContext().setAuthentication(authentication);
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String roleName = authorities.isEmpty() ? null : authorities.iterator().next().getAuthority();

        String token = jwtService.generateToken(authentication);

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setMessage("login success");
        response.setRole(USER_ROLE.valueOf(roleName));

        return response;
    }

    private Authentication authenticate(String username, String otp) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (userDetails == null)
            throw new BadCredentialsException("Invalid email");

        VerificationCode verificationCode = verificationCodeRepo.findByEmail(username);

        if (verificationCode == null || !verificationCode.getOtp().equals(otp))
            throw new BadCredentialsException("Wrong otp");

        verificationCodeRepo.delete(verificationCode);

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
