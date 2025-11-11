package com.rabbuy.ecommerce.service;

import com.rabbuy.ecommerce.dao.CartDao;
import com.rabbuy.ecommerce.dao.UserDao;
import com.rabbuy.ecommerce.dto.*; // 导入所有 DTO
import com.rabbuy.ecommerce.entity.Cart;
import com.rabbuy.ecommerce.entity.User;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.security.enterprise.identitystore.PasswordHash;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.lang.JoseException; // 导入

import java.time.OffsetDateTime; // 导入
import java.util.regex.Pattern;

@Singleton
public class UserServiceImpl implements UserService {

    @Inject
    private UserDao userDao;
    @Inject
    private CartDao cartDao;
    @Inject
    private PasswordHash passwordHash;
    @Inject
    private TokenService tokenService;

    // (toResponseDto 和 checkPasswordStrength 辅助方法保持不变)
    private UserResponseDto toResponseDto(User user) {
        return new UserResponseDto(
                user.getId(), user.getUsername(), user.getEmail(),
                user.getFirstName(), user.getLastName(), user.getPhone(),
                user.getProfilePicture(), user.isStaff()
        );
    }

    private boolean checkPasswordStrength(String password) {
        if (password == null) return false;
        int conditionsMet = 0;
        if (Pattern.compile("[A-Z]").matcher(password).find()) conditionsMet++;
        if (Pattern.compile("[a-z]").matcher(password).find()) conditionsMet++;
        if (Pattern.compile("[0-9]").matcher(password).find()) conditionsMet++;
        if (Pattern.compile("[^a-zA-Z0-9]").matcher(password).find()) conditionsMet++;
        if (password.length() >= 8) conditionsMet++;
        return conditionsMet >= 4;
    }

    @Override
    @Transactional
    public AuthResponseDto registerUser(UserSignupDto userDto) throws IllegalArgumentException, JoseException {

        // 1. 验证 (省略，与上一步相同)
        if (userDto.username() == null || userDto.email() == null || userDto.password() == null || userDto.confirmPwd() == null) {
            throw new IllegalArgumentException("All fields are required");
        }
        if (!userDto.password().equals(userDto.confirmPwd())) {
            throw new IllegalArgumentException("The two passwords do not match");
        }
        if (userDto.password().length() < 6 || userDto.password().length() > 20) {
            throw new IllegalArgumentException("Password must be 6-20 characters");
        }
        if (!checkPasswordStrength(userDto.password())) {
            throw new IllegalArgumentException("Password is too weak");
        }
        if (userDao.existsByUsername(userDto.username())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userDao.existsByEmail(userDto.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // 2. 哈希密码
        String hashedPassword = passwordHash.generate(userDto.password().toCharArray());

        // 3. 创建 User
        User newUser = new User();
        newUser.setUsername(userDto.username());
        newUser.setEmail(userDto.email());
        newUser.setFirstName(userDto.firstName() != null ? userDto.firstName() : "");
        newUser.setLastName(userDto.lastName() != null ? userDto.lastName() : "");
        newUser.setPassword(hashedPassword);
        newUser.setStaff(userDto.isStaff() != null && userDto.isStaff());

        // 4. 保存 User
        userDao.save(newUser);

        // 5. 创建购物车
         Cart newCart = new Cart();
         newCart.setUser(newUser);
         cartDao.save(newCart);

        // 6. 转换 DTO
        UserResponseDto userDtoResponse = toResponseDto(newUser);

        // 7. 返回 AuthResponseDto (包含 Token)
        return tokenService.generateTokens(newUser, userDtoResponse);
    }

    @Override
    @Transactional
    public AuthResponseDto loginUser(UserLoginDto loginDto) throws SecurityException, JoseException {

        User user = userDao.findByUsername(loginDto.username())
                .orElseThrow(() -> new SecurityException("Incorrect username or password"));

        boolean passwordVerified = passwordHash.verify(loginDto.password().toCharArray(), user.getPassword());

        if (!passwordVerified) {
            throw new SecurityException("Incorrect username or password");
        }

        if (!user.isActive()) {
            throw new SecurityException("User account is inactive");
        }

        // 4. *** 更新 last_login 时间戳 ***
        user.setLastLogin(OffsetDateTime.now());
        // (JPA 会在事务提交时自动保存此更新)

        UserResponseDto userDtoResponse = toResponseDto(user);

        // 6. 返回 AuthResponseDto (包含 Token)
        return tokenService.generateTokens(user, userDtoResponse);
    }

    @Override
    @Transactional
    public AuthResponseDto refreshUserToken(String refreshToken) throws JoseException, SecurityException, MalformedClaimException {
        //

        // 1. 验证 token 并获取 user ID
        String userId = tokenService.validateRefreshToken(refreshToken);

        // 2. 查找用户
        User user = userDao.findById(userId)
                .orElseThrow(() -> new SecurityException("User for token not found."));

        if (!user.isActive()) {
            throw new SecurityException("User account is inactive.");
        }

        // 3. 更新 last_login
        user.setLastLogin(OffsetDateTime.now());
        // (JPA 会在事务提交时自动保存此更新)

        // 4. 生成新 token
        UserResponseDto userDto = toResponseDto(user);
        return tokenService.generateTokens(user, userDto);
    }

    @Override
    @Transactional
    public AuthResponseDto updateUserProfile(String userId, UserProfileUpdateDto updateDto) throws JoseException, NotFoundException, IllegalArgumentException {
        //
        User user = userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // ... (省略验证和更新字段的逻辑)
        if (updateDto.username() != null && !updateDto.username().isEmpty()) {
            if (!user.getUsername().equals(updateDto.username()) && userDao.existsByUsername(updateDto.username())) {
                throw new IllegalArgumentException("Username already exists");
            }
            user.setUsername(updateDto.username());
        }
        if (updateDto.email() != null && !updateDto.email().isEmpty()) {
            if (!user.getEmail().equals(updateDto.email()) && userDao.existsByEmail(updateDto.email())) {
                throw new IllegalArgumentException("Email already exists");
            }
            user.setEmail(updateDto.email());
        }
        if (updateDto.phone() != null) user.setPhone(updateDto.phone());
        if (updateDto.profilePicture() != null) user.setProfilePicture(updateDto.profilePicture());
        // ...

        UserResponseDto userDtoResponse = toResponseDto(user);

        // 返回包含新 Token 的 AuthResponseDto
        return tokenService.generateTokens(user, userDtoResponse);
    }
}