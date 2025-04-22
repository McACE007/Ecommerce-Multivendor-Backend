package com.zosh.filter;

import com.zosh.constants.GlobalConstants;
import com.zosh.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService = new JwtService();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = request.getHeader(GlobalConstants.JWT_HEADER);

            if (!StringUtils.hasText(token) || !token.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            token = token.substring(7);

            if (!jwtService.validateToken(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            SecurityContextHolder.getContext().setAuthentication(jwtService.buildAuthenticationFromToken(token));

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BadCredentialsException("Invalid JWT Token...");
        }
    }
}
