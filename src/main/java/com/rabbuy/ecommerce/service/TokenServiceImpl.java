package com.rabbuy.ecommerce.service;

import com.rabbuy.ecommerce.dto.AuthResponseDto;
import com.rabbuy.ecommerce.dto.UserResponseDto;
import com.rabbuy.ecommerce.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collections;
import java.util.Base64;

@ApplicationScoped
public class TokenServiceImpl implements TokenService {

    // --- 1. 注入配置属性 ---

    @Inject
    @ConfigProperty(name = "jwt.secret.key")
    private String secretKeyString; // 从 .properties 文件注入

    @Inject
    @ConfigProperty(name = "jwt.lifetime.access.minutes", defaultValue = "15")
    private Integer accessTokenLifetimeMinutes; // 注入为 Integer

    @Inject
    @ConfigProperty(name = "jwt.lifetime.refresh.days", defaultValue = "7")
    private Integer refreshTokenLifetimeDays; // 注入为 Integer

    @Inject
    @ConfigProperty(name = "jwt.issuer", defaultValue = "com.rabbuy.ecommerce")
    private String issuer; // 注入签发者
    private Key signingKey = null;

    /**
     * 获取签名密钥 (使用注入的 secretKeyString)
     */
    private Key getSigningKey() {
        if (this.signingKey == null) {
            // 确保密钥足够长，满足 HS256 (至少 256 位 / 32 字节)
            if (secretKeyString == null || secretKeyString.getBytes(StandardCharsets.UTF_8).length < 32) {
                throw new RuntimeException("JWT Secret Key is not configured or is too short (must be >= 32 bytes)");
            }
            byte[] secretBytes = Base64.getDecoder().decode(secretKeyString);
            this.signingKey = new HmacKey(secretBytes);
        }
        return this.signingKey;
    }


    @Override
    public AuthResponseDto generateTokens(User user, UserResponseDto userDto) throws JoseException {

        // 3. 使用注入的有效期 (accessTokenLifetimeMinutes)
        JwtClaims accessClaims = buildClaims(user, accessTokenLifetimeMinutes.floatValue()); // 转为 float
        String accessToken = createJws(accessClaims);

        // 4. 使用注入的有效期 (refreshTokenLifetimeDays)
        float refreshLifetimeMinutes = refreshTokenLifetimeDays * 24 * 60; // 转换为分钟
        JwtClaims refreshClaims = buildClaims(user, refreshLifetimeMinutes);

        refreshClaims.unsetClaim("email");
        refreshClaims.unsetClaim("phone");
        refreshClaims.unsetClaim("is_staff");
        refreshClaims.setClaim("token_type", "refresh");
        String refreshToken = createJws(refreshClaims);

        return new AuthResponseDto(userDto, accessToken, refreshToken);
    }

    /**
     * 构建 JWT Claims (使用注入的 issuer)
     */
    private JwtClaims buildClaims(User user, float expirationTimeInMinutes) {
        JwtClaims claims = new JwtClaims();

        claims.setIssuer(this.issuer); // 5. 使用注入的签发者
        claims.setSubject(user.getId().toString());
        claims.setAudience("ecommerce-clients");
        claims.setIssuedAtToNow();
        claims.setExpirationTimeMinutesInTheFuture(expirationTimeInMinutes);
        claims.setGeneratedJwtId();

        claims.setClaim("username", user.getUsername());
        claims.setClaim("email", user.getEmail());
        claims.setClaim("phone", user.getPhone());
        claims.setClaim("is_staff", user.isStaff());

        claims.setClaim("groups", user.isStaff() ? Collections.singletonList("admin") : Collections.singletonList("customer"));

        return claims;
    }

    /**
     * 使用 HS256 签名并生成 JWS
     */
    private String createJws(JwtClaims claims) throws JoseException {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
        jws.setHeader("typ", "JWT");
        jws.setKey(getSigningKey());
        jws.setDoKeyValidation(false);

        return jws.getCompactSerialization();
    }

    @Override
    public String validateRefreshToken(String refreshToken) throws JoseException, SecurityException {
        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setRequireExpirationTime()
                .setAllowedClockSkewInSeconds(30)
                .setRequireSubject()
                .setExpectedIssuer(this.issuer)
                .setExpectedAudience("ecommerce-clients")
                .setVerificationKey(getSigningKey())
                .build();

        try {
            // 验证并解析 claims
            JwtClaims claims = jwtConsumer.processToClaims(refreshToken);
            String tokenType = claims.getStringClaimValue("token_type");
            if (!"refresh".equals(tokenType)) {
                throw new SecurityException("Invalid token type. Expected 'refresh'.");
            }
            return claims.getSubject();

        } catch (InvalidJwtException e) {
            throw new SecurityException("Refresh token is invalid or expired.", e);
        } catch (MalformedClaimException e) {
            throw new SecurityException("Refresh token claim is malformed.", e);
        }
    }
}