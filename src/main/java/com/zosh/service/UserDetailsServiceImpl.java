package com.zosh.service;

import com.zosh.constants.ExceptionMessages;
import com.zosh.domain.USER_ROLE;
import com.zosh.exception.SellerNotFoundException;
import com.zosh.exception.UserNotFoundException;
import com.zosh.model.Seller;
import com.zosh.model.User;
import com.zosh.repository.SellerRepo;
import com.zosh.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepo userRepo;
    private final SellerRepo sellerRepo;
    private static final String SELLER_PREFIX = "seller_";

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username.startsWith(SELLER_PREFIX)) {
            Seller seller = sellerRepo.findByEmail(username).orElseThrow(() -> {
                log.error(ExceptionMessages.SELLER_NOT_FOUND_EMAIL_DEV, username);
                return new SellerNotFoundException(ExceptionMessages.SELLER_NOT_FOUND_USER);
            });
            return buildUserDetails(seller.getEmail(), seller.getPassword(), seller.getRole());
        } else {
            User user = userRepo.findByEmail(username).orElseThrow(() -> {
                log.error(ExceptionMessages.USER_NOT_FOUND_EMAIL_DEV, username);
                return new UserNotFoundException(ExceptionMessages.USER_NOT_FOUND_USER);
            });
            return buildUserDetails(user.getEmail(), user.getPassword(), user.getRole());
        }
    }

    private UserDetails buildUserDetails(String email, String password, USER_ROLE role) {
        if (role == null) role = USER_ROLE.ROLE_CUSTOMER;

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role.toString()));

        return new org.springframework.security.core.userdetails.User(email, password, authorities);
    }
}
