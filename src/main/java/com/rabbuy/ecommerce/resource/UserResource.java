package com.rabbuy.ecommerce.resource;

import com.rabbuy.ecommerce.dto.*;
import com.rabbuy.ecommerce.service.UserService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.lang.JoseException;

import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.Context;

@Path("/user") // 基础路径 /api/user
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    private UserService userService;

    @Context
    private SecurityContext securityContext;


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
        return Response.status(Response.Status.CREATED).entity(ApiResponseDto.success(authResponse)).build();
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
        return Response.ok(ApiResponseDto.success(authResponse)).build();
    }

    /**
     * 刷新 Access Token
     * 对应: path('token-refresh/', ...)
     * 访问: POST /api/user/token-refresh
     */
    @POST
    @Path("/token-refresh")
    public Response refreshToken(TokenRefreshDto refreshDto) throws JoseException, MalformedClaimException {
        if (refreshDto == null || refreshDto.refresh() == null || refreshDto.refresh().isEmpty()) {
            // JAX-RS 异常会立即返回 400 响应
            throw new WebApplicationException("Refresh token is required", Response.Status.BAD_REQUEST);
        }
        // 业务异常 (SecurityException, JoseException) 将被 GlobalExceptionMapper 捕获
        AuthResponseDto authResponse = userService.refreshUserToken(refreshDto.refresh());
        return Response.ok(ApiResponseDto.success(authResponse)).build();
    }

    /**
     * 更新用户个人资料 (需要认证)
     * 对应: path('<uuid:id>/', ...)
     * 访问: PUT /api/user/{id} (必须携带 Bearer Token)
     */
    @PUT
    @Path("/{id}")
    public Response updateUserProfile(@PathParam("id") String id, UserProfileUpdateDto updateDto)
            throws JoseException {

        // **授权检查 (Authorization)**
        // 检查 1: 确保 JWT 已被注入
        if (securityContext.getUserPrincipal() == null || securityContext.getUserPrincipal().getName() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        // 检查 2: 用户是否是管理员？
        boolean isAdmin = securityContext.isUserInRole("admin");

        // 检查 3: 用户是否在尝试修改自己的个人资料？
        // jwtPrincipal.getName() 返回的是 'sub' 声明，即 User ID
        boolean isSelf = securityContext.getUserPrincipal().getName().equals(id);

        //
        // 业务逻辑：如果不是管理员，并且也不是在修改自己的资料，则禁止
        if (!isAdmin && !isSelf) {
            // 抛出 403 Forbidden
            throw new ForbiddenException("User is not authorized to update this profile.");
        }

        // 授权通过，执行业务逻辑
        AuthResponseDto authResponse = userService.updateUserProfile(id, updateDto);
        return Response.ok(ApiResponseDto.success(authResponse)).build();
    }
}