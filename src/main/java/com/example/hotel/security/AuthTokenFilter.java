package com.example.hotel.security;

import com.example.hotel.entity.Customer;
import com.example.hotel.repository.CustomerRepository;
import com.example.hotel.service.impl.UserAdminDetailsServiceImpl;
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
    private UserAdminDetailsServiceImpl userDetailsService; // Dịch vụ của Admin

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

                // LOGIC MỚI: Xác định loại người dùng dựa trên URL hoặc thử cả hai
                if (servletPath.startsWith("/api/admin") || servletPath.startsWith("/api/users")) {
                    // --- Xử lý cho ADMIN (giữ nguyên) ---
                    logger.info("--> AuthTokenFilter: Đang xử lý token ADMIN (path: {})", servletPath);
                    try {
                        userDetails = userDetailsService.loadUserByUsername(username);
                    } catch (UsernameNotFoundException e) {
                        logger.error("!!! Token ADMIN không hợp lệ. User không tồn tại: {}", username);
                        // Tùy chọn: Có thể trả về lỗi 401 ngay lập tức nếu muốn chặn triệt để
                        // response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token Admin không hợp lệ");
                        // return;
                    }
                } else {
                    // --- Xử lý cho CUSTOMER (cho TẤT CẢ các path còn lại, bao gồm /api/public/...) ---
                    // Logic này giả định: Nếu không phải là Admin thì thử xem có phải là Customer không.
                    // Điều này cho phép các API public nhận diện được khách hàng.

                    logger.info("--> AuthTokenFilter: Đang xử lý token CUSTOMER cho path public/khác: {}", servletPath);
                    // Tìm khách hàng trong DB
                    Customer customer = customerRepository.findByEmail(username).orElse(null);

                    if (customer != null) {
                        // Nếu tìm thấy khách hàng -> Tạo userDetails
                        userDetails = new User(
                                customer.getEmail(),
                                // Password có thể null nếu là khách vãng lai đăng ký nhanh, xử lý để tránh lỗi
                                customer.getPassword() != null ? customer.getPassword() : "",
                                new ArrayList<>() // Danh sách quyền (authorities) rỗng cho Customer
                        );
                    } else {
                        // Nếu token hợp lệ về mặt kỹ thuật nhưng không tìm thấy khách hàng trong DB
                        logger.warn("!!! Token hợp lệ nhưng không tìm thấy CUSTOMER với email: {}. Coi như khách vãng lai.", username);
                    }
                }

                // --- PHẦN CHUNG: NẾU XÁC ĐỊNH ĐƯỢC NGƯỜI DÙNG -> XÁC THỰC ---
                if (userDetails != null) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.info("--> Xác thực thành công cho '{}'. SecurityContext đã được cập nhật.", username);
                }
            }
        } catch (Exception e) {
            logger.error("!!! Lỗi không xác định trong AuthTokenFilter: {}", e.getMessage());
        }

        // Luôn cho request đi tiếp (để các cấu hình .permitAll() hoặc .authenticated() trong SecurityConfig xử lý tiếp)
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