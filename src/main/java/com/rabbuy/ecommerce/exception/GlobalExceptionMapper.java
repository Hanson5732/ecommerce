package com.rabbuy.ecommerce.exception;

import com.rabbuy.ecommerce.dto.ErrorResponseDto;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {

        // 1. 处理特定的业务异常
        if (exception instanceof IllegalArgumentException || exception instanceof IllegalStateException) {
            // 如 "密码不匹配", "库存不足", "不能删除有子分类的分类"
            ErrorResponseDto errorDto = new ErrorResponseDto(
                    Response.Status.BAD_REQUEST.getStatusCode(),
                    exception.getMessage()
            );
            return Response.status(Response.Status.BAD_REQUEST).entity(errorDto).build();
        }

        // 2. JAX-RS 的 NotFoundException
        if (exception instanceof NotFoundException) {
            ErrorResponseDto errorDto = new ErrorResponseDto(
                    Response.Status.NOT_FOUND.getStatusCode(),
                    exception.getMessage()
            );
            return Response.status(Response.Status.NOT_FOUND).entity(errorDto).build();
        }

        // 3. 处理安全异常（例如登录失败）
        if (exception instanceof SecurityException) {
            ErrorResponseDto errorDto = new ErrorResponseDto(
                    Response.Status.UNAUTHORIZED.getStatusCode(),
                    exception.getMessage()
            );
            return Response.status(Response.Status.UNAUTHORIZED).entity(errorDto).build();
        }

        // 4. 其他所有未处理的异常 (返回 500)
        exception.printStackTrace(); // 在服务器日志中打印堆栈
        ErrorResponseDto errorDto = new ErrorResponseDto(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "An unexpected error occurred."
        );
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDto).build();
    }
}