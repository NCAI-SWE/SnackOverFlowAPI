package com.snackOverFlow.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.snackOverFlow.config.minio.MinioProperties;
import com.snackOverFlow.exception.BusinessException;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MinioStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/svg+xml"
    );

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioProperties minioProperties;

    public String uploadMenuImage(String menuItemId, MultipartFile file) {
        validateFile(file);
        try {
            ensureBucketExists();
            String objectName = "menu-items/" + menuItemId + "/" + file.getOriginalFilename();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            String url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioProperties.getBucket())
                    .object(objectName)
                    .expiry(7, TimeUnit.DAYS)
                    .build());
            log.info("Uploaded image for menuItem={} objectName={}", menuItemId, objectName);
            return url;
        } catch (Exception ex) {
            log.error("Failed to upload image for menuItem={}: {}", menuItemId, ex.getMessage(), ex);
            throw new BusinessException("Failed to upload image: " + ex.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BusinessException("File must not be empty");
        if (file.getSize() > minioProperties.getMaxFileSizeBytes()) throw new BusinessException("File size exceeds maximum allowed size of 5MB");
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) throw new BusinessException("Unsupported file type. Allowed: JPEG, PNG, SVG");
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioProperties.getBucket()).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucket()).build());
            log.info("Created MinIO bucket: {}", minioProperties.getBucket());
        }
    }
}
