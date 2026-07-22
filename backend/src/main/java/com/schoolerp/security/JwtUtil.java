package com.schoolerp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return io.jsonwebtoken.security.Keys.hmacShaKeyFor(java.util.Arrays.copyOf(keyBytes, Math.max(keyBytes.length, 32)));
    }

    /**
     * Tenant (school) user token. schoolCode identifies which school's database this user
     * belongs to; jti is a fresh random id used purely for single-active-session enforcement
     * (see SessionService) - it is NOT a security nonce by itself, the signature still does
     * that job.
     */
    public String generateToken(UserDetails userDetails, String schoolCode, String jti) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(Object::toString)
                .orElse("ROLE_STAFF");

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .id(jti)
                .claim("role", role)
                .claim("school", schoolCode)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** Super-admin (platform) token - not tied to any school. */
    public String generateSuperAdminToken(String username, String jti) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .id(jti)
                .claim("role", "ROLE_SUPER_ADMIN")
                .claim("superAdmin", true)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    public String extractSchoolCode(String token) {
        return extractClaim(token, claims -> claims.get("school", String.class));
    }

    public boolean isSuperAdmin(String token) {
        return Boolean.TRUE.equals(extractClaim(token, claims -> claims.get("superAdmin", Boolean.class)));
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isTokenStructurallyValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }
}
