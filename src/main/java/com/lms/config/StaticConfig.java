package com.lms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StaticConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path scormPath = Paths.get("scorm-content").toAbsolutePath();

        System.out.println("SCORM PATH = " + scormPath);

        registry.addResourceHandler("/scorm-content/**")
                .addResourceLocations("file:" + scormPath + "/");
    }
}

