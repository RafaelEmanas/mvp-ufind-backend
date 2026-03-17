package com.ufind.ufindapp.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.ufind.ufindapp.config.R2Properties;
import com.ufind.ufindapp.dto.PresignedUploadDTO;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class FileService {

    private final R2Properties r2Properties;
    private final S3Presigner s3Presigner;


    public PresignedUploadDTO generatePutPresignedUrl(String contentType) {

        String fileName = UUID.randomUUID() + resolveExtension(contentType);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(r2Properties.getBucketName())
                .key(fileName)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(putObjectRequest)
                .build();

        String uploadUrl = s3Presigner
                .presignPutObject(presignRequest)
                .url()
                .toString();

        String imageUrl = r2Properties.getPublicUrl() + "/" + fileName;

        return new PresignedUploadDTO(uploadUrl, imageUrl);
    }

    private String resolveExtension(String contentType){

        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png"  -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new IllegalArgumentException("Type not supported: " + contentType);
        };

    }


}
