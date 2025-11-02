package com.community.property.config;

import com.community.property.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

// imports retained minimal; removed unused explicit jakarta servlet imports as fully-qualified names are used below
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request, 
                                    jakarta.servlet.http.HttpServletResponse response, 
                                    jakarta.servlet.FilterChain filterChain) throws jakarta.servlet.ServletException, IOException {
        
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;

        // 兼容大小写前缀与额外空格，同时兼容常见的替代头名称
        if (authorizationHeader != null) {
            String header = authorizationHeader.trim();
            if (header.regionMatches(true, 0, "Bearer ", 0, 7)) { // 忽略大小写匹配 "Bearer "
                token = header.substring(7).trim();
            } else if (header.regionMatches(true, 0, "Bearer", 0, 6)) { // 兼容缺少空格的写法
                token = header.substring(6).trim();
            }
        }
        if (token == null || token.isEmpty()) {
            // 兼容自定义头
            String alt = request.getHeader("token");
            if (alt == null || alt.isEmpty()) {
                alt = request.getHeader("access-token");
            }
            if (alt == null || alt.isEmpty()) {
                alt = request.getHeader("Authorization-Token");
            }
            if (alt != null && !alt.isEmpty()) {
                token = alt.trim();
            }
        }

        // 兼容 Authorization 直接为裸 JWT 的情况（无 Bearer 前缀）
        if ((token == null || token.isEmpty()) && authorizationHeader != null) {
            String raw = authorizationHeader.trim();
            // 简单判断是否可能是JWT（包含两处点号）
            int dotCount = 0;
            for (int i = 0; i < raw.length(); i++) {
                if (raw.charAt(i) == '.') dotCount++;
            }
            if (dotCount == 2) {
                token = raw;
            }
        }

        if (token != null && !token.isEmpty()) {
            
            try {
                if (!jwtUtil.isTokenExpired(token)) {
                    String username = jwtUtil.getUsernameFromToken(token);
                    String role = jwtUtil.getRoleFromToken(token);
                    
                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        // 创建包含角色信息的权限列表
                        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                        
                        UsernamePasswordAuthenticationToken authenticationToken = 
                                new UsernamePasswordAuthenticationToken(username, null, authorities);
                        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        
                        // 将角色信息添加到请求属性中
                        request.setAttribute("role", role);
                        
                        // 添加调试日志
                        logger.debug("Successfully authenticated user: " + username + " with role: " + role);
                    }
                } else {
                    logger.debug("Token is expired");
                }
            } catch (Exception e) {
                logger.error("无法验证JWT令牌", e);
            }
        } else {
            logger.debug("No valid Authorization header found");
        }
        
        filterChain.doFilter(request, response);
    }
}