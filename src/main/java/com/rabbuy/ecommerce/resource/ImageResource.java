package com.rabbuy.ecommerce.resource;

import com.rabbuy.ecommerce.dto.ApiResponseDto;
import com.rabbuy.ecommerce.dto.ImageUploadResponseDto;
import com.rabbuy.ecommerce.service.ImageService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;

@Path("/image")
@ApplicationScoped
public class ImageResource {

    @Inject
    private ImageService imageService;

    /**
     * 上传图片
     * @param request
     * @return
     */
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA) // 声明接收多部分表单数据
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadImage(@Context HttpServletRequest request) {
        try {
            Part filePart = request.getPart("file"); // 'file' 是表单字段的名称

            // 1. 验证文件是否存在
            if (filePart == null || filePart.getSize() == 0) {
                //
                throw new WebApplicationException("No file provided", Response.Status.BAD_REQUEST);
            }

            // 2. 验证 MIME 类型
            //
            if (filePart.getContentType() == null || !filePart.getContentType().startsWith("image/")) {
                throw new WebApplicationException("Only image files are allowed", Response.Status.BAD_REQUEST);
            }

            // 3. 调用服务层处理业务逻辑
            ImageUploadResponseDto responseDto = imageService.uploadImage(filePart);

            // 4. 返回 201 Created (与 Django 保持一致)
            return Response.status(Response.Status.CREATED).entity(ApiResponseDto.success(responseDto)).build();

        } catch (IllegalArgumentException e) {
            // 捕获来自 Service 的验证错误 (如大小、扩展名)
            throw new WebApplicationException(e.getMessage(), Response.Status.BAD_REQUEST);
        } catch (IOException | ServletException e) {
            // 捕获文件 IO 或 Servlet 错误
            throw new WebApplicationException("Error processing file upload: " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}