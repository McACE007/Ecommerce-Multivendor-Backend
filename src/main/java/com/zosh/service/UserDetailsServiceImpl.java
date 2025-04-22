package com.zosh.service;

import com.zosh.domain.USER_ROLE;
import com.zosh.model.Seller;
import com.zosh.model.User;
import com.zosh.repository.SellerRepo;
import com.zosh.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepo userRepo;
    private final SellerRepo sellerRepo;
    private static final String SELLER_PREFIX = "seller_";

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username.startsWith(SELLER_PREFIX)) {
            Seller seller = sellerRepo.findByEmail(username);

            if (seller == null)
                throw new UsernameNotFoundException("User not found");

            return buildUserDetails(seller.getEmail(), seller.getPassword(), seller.getRole());
        } else {
            User user = userRepo.findByEmail(username);

            if (user == null)
                throw new UsernameNotFoundException("User not found");

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
