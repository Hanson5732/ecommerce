package com.rabbuy.ecommerce.service;

import com.rabbuy.ecommerce.dto.ImageUploadResponseDto;
import jakarta.servlet.http.Part;
import java.io.IOException;

public interface ImageService {

    /**
     * 处理文件上传，包括验证和存储
     * @param filePart
     * @return
     * @throws IOException
     * @throws IllegalArgumentException
     */
    ImageUploadResponseDto uploadImage(Part filePart) throws IOException, IllegalArgumentException;
}