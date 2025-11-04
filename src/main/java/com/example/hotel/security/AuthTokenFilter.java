package com.example.hotel.security;

import com.example.hotel.security.UserDetailsServiceImpl;
import com.example.hotel.security.jwt.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // --- START: THÊM LOGGING ĐỂ DEBUG ---
        logger.info("====================================================================");
        logger.info("AuthTokenFilter is running for request: {}", request.getRequestURI());

        try {
            String jwt = parseJwt(request);
            if (jwt == null) {
                logger.warn("--> JWT Token is NULL. Header 'Authorization' might be missing or not start with 'Bearer '.");
            } else {
                logger.info("--> JWT Token found: {}", jwt);

                if (jwtUtils.validateJwtToken(jwt)) {
                    logger.info("--> JWT Token is valid.");
                    String username = jwtUtils.getUserNameFromJwtToken(jwt);
                    logger.info("--> Username from token: {}", username);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    logger.info("--> UserDetails loaded. Authorities: {}", userDetails.getAuthorities());

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.info("--> User '{}' authenticated successfully. SecurityContext updated.", username);
                } else {
                    logger.warn("--> JWT Token is NOT valid.");
                }
            }
        } catch (Exception e) {
            logger.error("!!! Cannot set user authentication: {}", e.getMessage(), e);
        }

        logger.info("====================================================================");
        // --- END: THÊM LOGGING ĐỂ DEBUG ---

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (headerAuth == null) {
            logger.warn("--> Header 'Authorization' is missing.");
            return null;
        }
        logger.info("--> Authorization Header: {}", headerAuth);

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }
}
