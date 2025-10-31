package com.rabbuy.ecommerce.service;

import com.rabbuy.ecommerce.dto.ImageUploadResponseDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.Part;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ImageServiceImpl implements ImageService {

    // 允许的扩展名
    private static final List<String> ALLOWED_EXTENSIONS = List.of(".jpg", ".jpeg", ".png", ".gif");
    // 文件大小限制 (20MB)，与 Django 一致
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;

    @Inject
    @ConfigProperty(name = "media.upload.path")
    private String mediaUploadPath;

    @Inject
    @ConfigProperty(name = "media.base.url")
    private String mediaBaseUrl;

    @Override
    public ImageUploadResponseDto uploadImage(Part filePart) throws IOException, IllegalArgumentException {

        String originalFileName = filePart.getSubmittedFileName();
        long fileSize = filePart.getSize();
        String fileExtension = getFileExtension(originalFileName);

        // 1. 验证 (MIME type 在 Resource 层已检查)
        //
        validateFile(originalFileName, fileSize);

        // 2. 生成唯一文件名
        String uniqueFileName = generateUniqueFileName(fileExtension);

        // 3. 准备目标路径
        Path uploadDir = Paths.get(mediaUploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        Path destination = uploadDir.resolve(uniqueFileName);

        // 4. 【已修复】验证内容并净化 (剥离 EXIF)
        try (InputStream fileStream = filePart.getInputStream()) {

            // 对应 Django: Image.open(file)
            BufferedImage bufferedImage = ImageIO.read(fileStream);

            // 对应 Django: Image.open(file).verify()
            if (bufferedImage == null) {
                //
                throw new IllegalArgumentException("Invalid image file: content cannot be read.");
            }

            // 对应 Django: img.save(...) (剥离 EXIF)
            String formatName = fileExtension.substring(1).toLowerCase();
            if ("jpg".equals(formatName)) {
                formatName = "jpeg";
            }

            boolean success = ImageIO.write(bufferedImage, formatName, destination.toFile());

            if (!success) {
                throw new IOException("Failed to write image to disk for format: " + formatName);
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid image processing: " + e.getMessage());
        }

        // 5. 构建返回 URL
        String fileUrl = mediaBaseUrl + "/" + uniqueFileName;

        return new ImageUploadResponseDto(fileUrl);
    }

    private void validateFile(String fileName, long fileSize) throws IllegalArgumentException {
        // 验证文件大小
        if (fileSize > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size must be less than 20MB");
        }

        // 验证文件扩展名
        String extension = getFileExtension(fileName);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file extension. Allowed: " + ALLOWED_EXTENSIONS);
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            throw new IllegalArgumentException("Invalid file name (missing extension)");
        }
        return fileName.substring(fileName.lastIndexOf('.'));
    }

    private String generateUniqueFileName(String extension) {
        //
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return timestamp + "_" + uuid + extension;
    }
}