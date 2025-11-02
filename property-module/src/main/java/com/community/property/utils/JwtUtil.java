package com.community.property.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret:mySecretKeyForCommunitySystemWhichIsVeryLongAndSecure}")
    private String secret;
    
    @Value("${jwt.expiration:86400000}")
    private Long expiration;
    
    /**
     * 生成JWT Token (支持staffId)
     * @param username 用户名
     * @param role 角色(owner/staff)
     * @param staffId 员工ID
     * @return JWT Token
     */
    public String generateToken(String username, String role, Long staffId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        if (staffId != null) {
            claims.put("staffId", staffId);
        }
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    /**
     * 获取签名密钥
     * @return SecretKey
     */
    private SecretKey getSigningKey() {
        // 基于配置的 secret 生成稳定且满足 HS512 要求的密钥
        // 使用 SHA-512 对 secret 做哈希，得到固定 64 字节的 key，避免因随机密钥导致重启后签名不一致
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(keyBytes); // 64 bytes
            return Keys.hmacShaKeyFor(hash);
        } catch (Exception e) {
            // 兜底：直接使用原始字节（在绝大多数情况下也可用）
            return Keys.hmacShaKeyFor(keyBytes);
        }
    }
    
    /**
     * 解析JWT Token获取Claims
     * @param token JWT Token
     * @return Claims
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 从Token中获取用户名
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }
    
    /**
     * 从Token中获取角色
     * @param token JWT Token
     * @return 角色
     */
    public String getRoleFromToken(String token) {
        return (String) getClaimsFromToken(token).get("role");
    }
    
    /**
     * 从Token中获取员工ID
     * @param token JWT Token
     * @return 员工ID
     */
    public Long getStaffIdFromToken(String token) {
        Object staffId = getClaimsFromToken(token).get("staffId");
        if (staffId instanceof Number) {
            return ((Number) staffId).longValue();
        }
        return null;
    }
    
    /**
     * 验证Token是否过期
     * @param token JWT Token
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        Date expiration = getClaimsFromToken(token).getExpiration();
        return expiration.before(new Date());
    }
    
    /**
     * 验证Token有效性
     * @param token JWT Token
     * @param username 用户名
     * @return Token是否有效
     */
    public boolean validateToken(String token, String username) {
        String tokenUsername = getUsernameFromToken(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }
}