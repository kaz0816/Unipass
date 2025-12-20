package com.example.demo;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // プロジェクト直下の "uploads" フォルダを /uploads/** で配信
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
