package com.rabbuy.ecommerce.resource;

import com.rabbuy.ecommerce.dto.AuthResponseDto;
import com.rabbuy.ecommerce.dto.TokenRefreshDto; // 导入
import com.rabbuy.ecommerce.dto.UserLoginDto;
import com.rabbuy.ecommerce.dto.UserSignupDto;
import com.rabbuy.ecommerce.service.UserService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jose4j.lang.JoseException;

@Path("/user") // 基础路径 /api/user
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    private UserService userService;

    /**
     * 注册新用户
     * 对应: path('register/', ...)
     * 访问: POST /api/user/register
     */
    @POST
    @Path("/register")
    public Response registerUser(UserSignupDto userDto) throws JoseException {
        // 业务异常 (IllegalArgumentException) 将被 GlobalExceptionMapper 捕获
        AuthResponseDto authResponse = userService.registerUser(userDto);
        return Response.status(Response.Status.CREATED).entity(authResponse).build();
    }

    /**
     * 用户登录
     * 对应: path('login/', ...)
     * 访问: POST /api/user/login
     */
    @POST
    @Path("/login")
    public Response loginUser(UserLoginDto loginDto) throws JoseException {
        // 业务异常 (SecurityException) 将被 GlobalExceptionMapper 捕获
        AuthResponseDto authResponse = userService.loginUser(loginDto);
        return Response.ok(authResponse).build();
    }

    /**
     * 刷新 Access Token
     * 对应: path('token-refresh/', ...)
     * 访问: POST /api/user/token-refresh
     */
    @POST
    @Path("/token-refresh")
    public Response refreshToken(TokenRefreshDto refreshDto) throws JoseException {
        if (refreshDto == null || refreshDto.refresh() == null || refreshDto.refresh().isEmpty()) {
            // JAX-RS 异常会立即返回 400 响应
            throw new WebApplicationException("Refresh token is required", Response.Status.BAD_REQUEST);
        }
        // 业务异常 (SecurityException, JoseException) 将被 GlobalExceptionMapper 捕获
        AuthResponseDto authResponse = userService.refreshUserToken(refreshDto.refresh());
        return Response.ok(authResponse).build();
    }

    //
    // 下一步：我们将实现需要认证的端点
    //
}