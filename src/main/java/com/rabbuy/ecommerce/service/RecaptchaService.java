package com.rabbuy.ecommerce.service;

public interface RecaptchaService {
    /**
     * 验证 Google reCAPTCHA v3 token
     * @param token The token from the client
     * @param action The action expected (e.g., 'login', 'register')
     * @return true if verification succeeds
     * @throws SecurityException if verification fails
     */
    boolean verify(String token, String action) throws SecurityException;
}