package com.lms.content.service;

import com.lms.common.exception.LmsException;
import com.lms.content.config.MinioConfig;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    @PostConstruct
    public void init() {
        createBucketIfNotExists(minioConfig.getBucketName());
    }

    public void createBucketIfNotExists(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                log.info("Created bucket: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("Error creating bucket: {}", e.getMessage());
            throw new LmsException("Failed to create bucket", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String uploadFile(String bucketName, String objectName, MultipartFile file) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            
            log.info("File uploaded: {}/{}", bucketName, objectName);
            return objectName;
        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage());
            throw new LmsException("Failed to upload file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String uploadFile(String bucketName, String objectName, InputStream inputStream, 
                             long size, String contentType) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build());
            
            log.info("File uploaded: {}/{}", bucketName, objectName);
            return objectName;
        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage());
            throw new LmsException("Failed to upload file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public InputStream downloadFile(String bucketName, String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("Error downloading file: {}", e.getMessage());
            throw new LmsException("Failed to download file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String getPresignedUploadUrl(String bucketName, String objectName, int expiryMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(expiryMinutes, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            log.error("Error generating presigned upload URL: {}", e.getMessage());
            throw new LmsException("Failed to generate upload URL", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String getPresignedDownloadUrl(String bucketName, String objectName, int expiryMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(expiryMinutes, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            log.error("Error generating presigned download URL: {}", e.getMessage());
            throw new LmsException("Failed to generate download URL", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void deleteFile(String bucketName, String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            log.info("File deleted: {}/{}", bucketName, objectName);
        } catch (Exception e) {
            log.error("Error deleting file: {}", e.getMessage());
            throw new LmsException("Failed to delete file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public StatObjectResponse getObjectInfo(String bucketName, String objectName) {
        try {
            return minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("Error getting object info: {}", e.getMessage());
            throw new LmsException("Failed to get file info", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public boolean objectExists(String bucketName, String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
