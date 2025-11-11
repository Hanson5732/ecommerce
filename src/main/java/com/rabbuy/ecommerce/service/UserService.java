package com.rabbuy.ecommerce.service;

import com.rabbuy.ecommerce.dto.AuthResponseDto;
import com.rabbuy.ecommerce.dto.UserLoginDto;
import com.rabbuy.ecommerce.dto.UserProfileUpdateDto;
import com.rabbuy.ecommerce.dto.UserSignupDto;
import jakarta.transaction.Transactional;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.lang.JoseException;
import jakarta.ws.rs.NotFoundException;

public interface UserService {

    /**
     * 注册新用户并返回 Token
     */
    AuthResponseDto registerUser(UserSignupDto userDto) throws IllegalArgumentException, JoseException;

    /**
     * 用户登录, 更新 last_login, 并返回 Token
     */
    @Transactional // 登录现在包含数据库写入 (last_login)
    AuthResponseDto loginUser(UserLoginDto loginDto) throws SecurityException, JoseException;

    /**
     * 使用 Refresh Token 刷新 Access Token
     * 也会更新 last_login
     */
    @Transactional
    AuthResponseDto refreshUserToken(String refreshToken) throws JoseException, SecurityException, MalformedClaimException;

    /**
     * 更新用户个人资料并返回新 Token
     */
    @Transactional // 确保 update 是事务性的
    AuthResponseDto updateUserProfile(String userId, UserProfileUpdateDto updateDto) throws JoseException, NotFoundException, IllegalArgumentException;
}