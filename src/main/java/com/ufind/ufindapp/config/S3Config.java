package com.ufind.ufindapp.config;

import java.net.URI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@RequiredArgsConstructor
public class S3Config {

        private final R2Properties r2Properties;

        @Bean
        public S3Presigner s3Presigner() {
                return S3Presigner.builder()
                        .endpointOverride(URI.create(r2Properties.getEndpoint()))
                        .credentialsProvider(StaticCredentialsProvider.create(
                                        AwsBasicCredentials.create(r2Properties.getAccessKeyId(), r2Properties.getSecretAccessKey())
                                )
                        )
                        .region(Region.of(r2Properties.getRegion()))
                        .serviceConfiguration(S3Configuration.builder()
                                .pathStyleAccessEnabled(true)
                                .build())
                        .build();
        }


}
