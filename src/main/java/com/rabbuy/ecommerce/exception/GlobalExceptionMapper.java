package com.rabbuy.ecommerce.exception;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import com.rabbuy.ecommerce.dto.ApiResponseDto;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        int status;
        String message;

        // 1. 捕获特定的业务异常 (客户端错误)
        if (exception instanceof IllegalArgumentException) {
            // 400 Bad Request (例如: "Username already exists", "Password is too weak")
            status = Response.Status.BAD_REQUEST.getStatusCode();
            message = exception.getMessage();
            logger.info("Bad Request (400): {}", message); // 作为普通信息记录，而不是错误

        } else if (exception instanceof SecurityException) {
            // 401 Unauthorized (例如: "Incorrect username or password")
            status = Response.Status.UNAUTHORIZED.getStatusCode();
            message = exception.getMessage();
            logger.info("Unauthorized (401): {}", message); // 作为普通信息记录

            // 2. 捕获特定的 JAX-RS 异常 (客户端错误)
        } else if (exception instanceof NotFoundException) {
            // 404 Not Found
            status = Response.Status.NOT_FOUND.getStatusCode();
            message = "The requested resource was not found."; // 统一的 404 消息
            logger.info("Not Found (404): {}", exception.getMessage());

        } else if (exception instanceof ForbiddenException) {
            // 403 Forbidden (例如: 用户无权更新个人资料)
            status = Response.Status.FORBIDDEN.getStatusCode();
            message = exception.getMessage(); // 使用服务层提供的消息
            logger.info("Forbidden (403): {}", message);

        } else if (exception instanceof WebApplicationException) {
            // 3. 捕获其他所有 JAX-RS 异常
            WebApplicationException webEx = (WebApplicationException) exception;
            status = webEx.getResponse().getStatus();
            message = webEx.getMessage();
            logger.warn("Web Application Exception ({}): {}", status, message);

            // 4. 捕获所有意外的服务器错误
        } else {
            // 500 Internal Server Error
            status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
            message = "An internal server error occurred.";
            // 这是真正的服务器错误，记录完整的堆栈跟踪
            logger.error("Unexpected server error (500): {}", exception.getMessage(), exception);
        }

        // 统一使用 ApiResponseDto 格式返回
        ApiResponseDto<?> errorResponse = ApiResponseDto.error(message);

        return Response.status(status)
                .entity(errorResponse)
                .type(MediaType.APPLICATION_JSON) // 确保指定了类型
                .build();
    }
}