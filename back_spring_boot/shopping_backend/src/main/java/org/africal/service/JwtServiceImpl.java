package org.africal.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service("jwtService")
public class JwtServiceImpl implements JwtService {

    /**
     * JWT 토큰 생성 및 검증에 사용되는 비밀 키입니다.
     * HS256 알고리즘에서 요구하는 256비트 이상의 강력한 키를 생성합니다.
     * 비밀 키는 고정된 문자열이 아닌 Keys 클래스를 통해 동적으로 생성됩니다.
     * 필요에 따라 생성된 키를 환경 변수나 설정 파일에 저장하여 재사용할 수도 있습니다.
     */
    private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    /**
     * 주어진 key-value를 기반으로 JWT 토큰을 생성합니다.
     */
    @Override
    public String getToken(String key, Object value) {
        try {
            // 토큰 만료 시간 설정 (현재 시간 + 30분)
            Date expTime = new Date(System.currentTimeMillis() + 1000 * 60 * 30);

            // JWT 헤더 설정
            Map<String, Object> headerMap = new HashMap<>();
            headerMap.put("typ", "JWT");
            headerMap.put("alg", "HS256");

            // JWT 페이로드 설정
            Map<String, Object> claimsMap = new HashMap<>();
            claimsMap.put(key, value);

            // JWT 생성
            return Jwts.builder()
                    .setHeader(headerMap)
                    .setClaims(claimsMap)
                    .setExpiration(expTime)
                    .signWith(secretKey) // 강력한 비밀 키 사용
                    .compact();
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Error while generating token", e);
        }
    }

    /**
     * 주어진 토큰에서 클레임(Claims)을 추출합니다.
     */
    @Override
    public Claims getClaims(String token) {
        if (token != null && !token.isEmpty()) {
            try {
                return Jwts.parserBuilder()
                        .setSigningKey(secretKey)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
            } catch (ExpiredJwtException e) {
                System.out.println("Token expired: " + e.getMessage());
            } catch (JwtException e) {
                System.out.println("Invalid token: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * 주어진 토큰이 유효한지 확인합니다.
     */
    @Override
    public boolean isValid(String token) {
        return this.getClaims(token) != null;
    }

    /**
     * 주어진 토큰에서 "id" 클레임 값을 추출합니다.
     */
    @Override
    public int getId(String token) {
        Claims claims = this.getClaims(token);
        if (claims != null) {
            try {
                return Integer.parseInt(claims.get("id").toString());
            } catch (NumberFormatException e) {
                System.out.println("Invalid id format: " + e.getMessage());
            }
        }
        return 0;
    }
}
