package com.rabbuy.ecommerce.exception;

import com.rabbuy.ecommerce.dto.ApiResponseDto; // 导入我们的 DTO
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
        int status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        String message;

        if (exception instanceof WebApplicationException) {
            WebApplicationException webEx = (WebApplicationException) exception;
            status = webEx.getResponse().getStatus();
            message = webEx.getMessage();
        } else {
            logger.error("Unexpected server error: {}", exception.getMessage(), exception);
            message = "An internal server error occurred.";
        }

        // 使用新的 ApiResponseDto.error() 格式
        ApiResponseDto<?> errorResponse = ApiResponseDto.error(message);

        // 返回原始的 HTTP 状态码 (例如 404, 400)，
        // 但 body 是我们自定义的 JSON 结构
        return Response.status(status)
                .entity(errorResponse)
                .build();
    }
}