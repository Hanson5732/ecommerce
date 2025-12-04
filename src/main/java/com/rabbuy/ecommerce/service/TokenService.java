package com.rabbuy.ecommerce.service;

import com.rabbuy.ecommerce.dto.AuthResponseDto;
import com.rabbuy.ecommerce.dto.UserResponseDto;
import com.rabbuy.ecommerce.entity.User;
import org.jose4j.lang.JoseException;
import org.jose4j.jwt.MalformedClaimException;

public interface TokenService {

    /**
     * 为指定用户生成 Access Token 和 Refresh Token
     *
     * @param user 用户实体
     * @return 包含 tokens 和 user DTO 的 AuthResponseDto
     * @throws JoseException 如果 token 签名失败
     */
    AuthResponseDto generateTokens(User user, UserResponseDto userDto) throws JoseException;

    /**
     * 验证一个 Refresh Token
     *
     * @param refreshToken The refresh token string
     * @return 成功时返回 User ID (Subject)
     * @throws JoseException     如果 token 签名无效、过期或格式错误
     * @throws SecurityException 如果 token 不是一个 'refresh' 类型的 token
     */
    String validateRefreshToken(String refreshToken) throws JoseException, SecurityException, MalformedClaimException;
}