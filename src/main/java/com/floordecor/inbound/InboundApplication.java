package com.floordecor.inbound;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = {"com.floordecor.inbound", "com.supplychain","com.supplychain.mawm"})
@EnableConfigurationProperties
@EnableIntegration
@IntegrationComponentScan(basePackages = {"com.floordecor.inbound", "com.supplychain","com.supplychain.mawm"})
@EnableBatchProcessing
@EnableScheduling
@EnableAsync
@EnableSpringConfigured
@EnableFeignClients(basePackages = {"com.floordecor.inbound", "com.supplychain","com.supplychain.mawm"})
public class InboundApplication {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.default", "dev");
        SpringApplication.run(InboundApplication.class, args);
    }
}
