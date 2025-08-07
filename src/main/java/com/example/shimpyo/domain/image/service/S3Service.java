package com.example.shimpyo.domain.image.service;

import com.example.shimpyo.domain.image.dto.ImageRequestDto;
import com.example.shimpyo.global.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

import static com.example.shimpyo.global.exceptionType.AuthException.FILE_SIZE_OVER;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public String generatePresignedUrl(ImageRequestDto requestDto) {

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(10))  // URL 유효 시간
                        .putObjectRequest(PutObjectRequest.builder().bucket(bucketName).key(requestDto.getFileName()).build())
                        .build()
        );

        return presignedRequest.url().toString();

    }
}
