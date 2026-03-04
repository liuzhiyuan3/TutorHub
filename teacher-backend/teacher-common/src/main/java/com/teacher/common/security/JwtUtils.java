package com.teacher.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtils {
    private final JwtProperties jwtProperties;

    public JwtUtils(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateToken(LoginUser loginUser) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", loginUser.getId());
        claims.put("account", loginUser.getAccount());
        claims.put("userType", loginUser.getUserType());
        claims.put("admin", loginUser.isAdmin());
        Instant now = Instant.now();
        Instant expireAt = now.plus(jwtProperties.getExpireHours(), ChronoUnit.HOURS);
        return Jwts.builder()
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .signWith(secretKey())
                .compact();
    }

    public LoginUser parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        LoginUser loginUser = new LoginUser();
        loginUser.setId((String) claims.get("uid"));
        loginUser.setAccount((String) claims.get("account"));
        Object type = claims.get("userType");
        if (type instanceof Integer i) {
            loginUser.setUserType(i);
        } else if (type instanceof Number n) {
            loginUser.setUserType(n.intValue());
        }
        Object admin = claims.get("admin");
        loginUser.setAdmin(Boolean.TRUE.equals(admin));
        return loginUser;
    }

    private SecretKey secretKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes.length >= 32 ? keyBytes : padTo32(keyBytes));
    }

    private byte[] padTo32(byte[] src) {
        byte[] target = new byte[32];
        for (int i = 0; i < target.length; i++) {
            target[i] = src[i % src.length];
        }
        return target;
    }
}
