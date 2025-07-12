package com.example.shimpyo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("쉼표 API 명세서")
                        .description("API 명세서")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes("AccessToken",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT"))
                        .addSchemas("UserLoginRequestDto", createUserLoginRequestDtoSchema())
                        .addSchemas("LoginResponse", createLoginResponseSchema()))
                .addSecurityItem(new SecurityRequirement().addList("AccessToken"))
                .path("/users/login", createLoginPath());
    }

    private PathItem createLoginPath() {
        Operation loginOperation = new Operation()
                .summary("User Login")
                .description("Authenticate user and return JWT tokens")
                .tags(java.util.List.of("Authentication"))
                .requestBody(new RequestBody()
                        .content(new Content()
                                .addMediaType("application/json", new MediaType()
                                        .schema(new Schema<>().$ref("#/components/schemas/UserLoginRequestDto")))))
                .responses(new ApiResponses()
                        .addApiResponse("200", new ApiResponse()
                                .description("Successful login")
                                .content(new Content()
                                        .addMediaType("application/json", new MediaType()
                                                .schema(new Schema<>().$ref("#/components/schemas/LoginResponse"))))));

        return new PathItem().post(loginOperation);
    }

    private Schema<?> createUserLoginRequestDtoSchema() {
        return new Schema<>()
                .type("object")
                .addProperty("email", new Schema<>().type("string"))
                .addProperty("password", new Schema<>().type("string"));
    }

    private Schema<?> createLoginResponseSchema() {
        return new Schema<>()
                .type("object")
                .addProperty("accessToken", new Schema<>().type("string"))
                .addProperty("refreshToken", new Schema<>().type("string"));
    }
}