package com.oran.oranpicturebackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai.qwen")
public class QwenConfig {
    
    private String apiKey;
    
    private String model = "qwen3-vl-flash-2026-01-22";
    
    private Integer timeout = 30000;
    
    private Float temperature = 0.1f;
}