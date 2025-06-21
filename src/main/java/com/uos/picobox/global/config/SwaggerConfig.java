package com.uos.picobox.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI(@Value("${spring.application.name:PicoBox API}") String appName) {
        Info info = new Info()
                .title(appName)
                .version("v0.0.1")
                .description("PicoBox 영화 예매 백엔드 애플리케이션 API 명세서입니다.")
                .license(new License()
                        .name("Apache License Version 2.0")
                        .url("http://www.apache.org/licenses/LICENSE-2.0"));

        // 🔒 SecurityScheme 설정
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("세션 ID를 Authorization 헤더에 입력하세요");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("sessionAuth");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("sessionAuth", securityScheme))
                .addSecurityItem(securityRequirement)
                .info(info);
    }
}