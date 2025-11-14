package com.example.hotel.security;

import com.example.hotel.entity.Customer;
import com.example.hotel.repository.CustomerRepository;
import com.example.hotel.service.impl.UserDetailsServiceImpl;
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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService; // Dịch vụ của Admin

    @Autowired
    private CustomerRepository customerRepository; // Repo của Customer

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        String servletPath = path.substring(contextPath.length());

        try {
            String jwt = parseJwt(request);
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                String username = jwtUtils.getUserNameFromJwtToken(jwt);
                UserDetails userDetails = null;

                if (servletPath.startsWith("/api/admin") || servletPath.startsWith("/api/users")) {

                    logger.info("--> AuthTokenFilter: Đang xử lý token cho ADMIN (path: {})", servletPath);
                    try {
                        userDetails = userDetailsService.loadUserByUsername(username);
                    } catch (UsernameNotFoundException e) {
                        logger.error("!!! Token ADMIN không hợp lệ. Không tìm thấy User (Admin): {}", username);
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token Admin không hợp lệ");
                        return;
                    }
                }
                else if (servletPath.startsWith("/api/public/customer")) {
                    logger.info("--> AuthTokenFilter: Đang xử lý token cho CUSTOMER (path: {})", servletPath);
                    Customer customer = customerRepository.findByEmail(username)
                            .orElseThrow(() -> new UsernameNotFoundException("Token CUSTOMER không hợp lệ: " + username));

                    userDetails = new User(
                            customer.getEmail(),
                            customer.getPassword() != null ? customer.getPassword() : "",
                            new ArrayList<>()
                    );
                }
                else {
                    logger.warn("--> AuthTokenFilter: Bỏ qua (cho phép) đường dẫn public: {}", servletPath);
                }

                // Nếu userDetails được tạo (từ Admin hoặc Customer) -> Xác thực
                if (userDetails != null) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.info("--> Xác thực thành công cho '{}'. Đã cập nhật SecurityContext.", username);
                }
            }
        } catch (Exception e) {
            logger.error("!!! Không thể xác thực người dùng (AuthTokenFilter): {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}