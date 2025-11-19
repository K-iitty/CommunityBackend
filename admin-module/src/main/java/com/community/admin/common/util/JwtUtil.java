package com.community.admin.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 
 * 提供JWT Token的生成、解析和验证功能
 * 用于用户身份认证和权限控制
 * 
 * 关键点:
 * 1. 使用io.jsonwebtoken库实现JWT操作
 * 2. 通过@Value注解从配置文件读取密钥和过期时间
 * 3. 使用HS512签名算法保证Token安全性
 * 4. 自动处理密钥长度不足的情况
 * 
 * 难点说明:
 * 1. 密钥长度要求: HS512算法要求密钥至少512位(64字节)
 * 2. 线程安全性: 每次生成Token都创建新的SecretKey实例
 * 3. 过期处理: 正确处理Token过期情况，避免安全风险
 */
@Component
public class JwtUtil {

    /**
     * Token过期时间(毫秒)
     * 默认值: 86400000ms (24小时)
     */
    @Value("${jwt.expiration:86400000}")
    private long expiration;

    /**
     * 签名密钥
     * 默认值: communitySystemSecretKeyForJWTTokenGenerationAndValidation
     */
    @Value("${jwt.secret:communitySystemSecretKeyForJWTTokenGenerationAndValidation}")
    private String secret;

    /**
     * 获取签名密钥
     * 
     * 难点说明:
     * 1. HS512算法要求密钥至少512位(64字节)
     * 2. 如果配置的密钥长度不足，需要进行填充处理
     * 3. 使用Keys.hmacShaKeyFor方法生成符合规范的SecretKey
     * 
     * @return 符合HS512算法要求的SecretKey实例
     */
    private SecretKey getSigningKey() {
        // 确保密钥长度至少为512位（64字节）
        byte[] keyBytes;
        if (secret.length() < 64) {
            // 如果密钥长度不足，进行填充
            keyBytes = new byte[64];
            byte[] secretBytes = secret.getBytes();
            System.arraycopy(secretBytes, 0, keyBytes, 0, Math.min(secretBytes.length, keyBytes.length));
        } else {
            keyBytes = secret.getBytes();
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成JWT Token
     * 
     * @param username 用户名
     * @return 生成的JWT Token字符串
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 从Token中解析Claims
     * 
     * @param token JWT Token
     * @return Claims对象，包含Token中的所有声明信息
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
     * 
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * 判断Token是否已过期
     * 
     * @param token JWT Token
     * @return true表示已过期，false表示未过期
     */
    public Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getClaimsFromToken(token).getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true; // 解析失败视为过期
        }
    }

    /**
     * 验证Token有效性
     * 
     * @param token JWT Token
     * @param username 用户名
     * @return true表示有效，false表示无效
     */
    public Boolean validateToken(String token, String username) {
        try {
            final String tokenUsername = getUsernameFromToken(token);
            return (tokenUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }
}