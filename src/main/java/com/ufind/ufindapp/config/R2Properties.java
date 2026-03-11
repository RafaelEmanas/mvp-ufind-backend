package com.ufind.ufindapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "cloudflare.r2")
@Data
public class R2Properties {
    private String endpoint;
    private String accessKeyId;
    private String secretAccessKey;
    private String bucketName;
    private String region;
}
