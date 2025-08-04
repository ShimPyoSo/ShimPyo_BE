package com.example.shimpyo.config;

import com.example.shimpyo.global.ExampleHolder;
import com.example.shimpyo.global.SwaggerErrorApi;
import com.example.shimpyo.global.exceptionType.ExceptionType;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
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
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.util.*;

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
                .path("/api/user/auth/login", createLoginPath());
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

    @Bean
    public OperationCustomizer customize() {
        return (Operation operation, HandlerMethod handlermethod) -> {
            SwaggerErrorApi swaggerErrorApi = handlermethod.getMethodAnnotation(SwaggerErrorApi.class);

            if (swaggerErrorApi != null) {
                generateMultiErrorCodeResponse(operation, swaggerErrorApi);

            }
            return operation;
        };
    }

    private void generateMultiErrorCodeResponse(Operation operation, SwaggerErrorApi annotation) {

        ApiResponses responses = operation.getResponses();

        Class<? extends ExceptionType>[] types = annotation.type();
        Set<String> allowedCodes = new HashSet<>(Arrays.asList(annotation.codes()));

        for (Class<? extends ExceptionType> type : types) {
            ExceptionType[] errorCodes = type.getEnumConstants();
            if (errorCodes == null) continue; // 혹시 enum이 아닌 클래스가 들어온 경우 방지

            for (ExceptionType errorCode : errorCodes) {
                if (!allowedCodes.contains(errorCode.name())) continue;

                ExampleHolder exampleHolder = ExampleHolder.builder()
                        .holder(getSwaggerExample(errorCode))
                        .name(errorCode.name())
                        .code(errorCode.httpStatus().value())
                        .build();

                addExamplesToResponses(responses, exampleHolder);
            }
        }
    }

    private Map<String, Example> getSwaggerExample(ExceptionType errorCode) {
        Example example = new Example();
        example.setSummary(errorCode.name());
        example.setDescription(errorCode.message());
        example.setValue(Map.of(
                "status", errorCode.httpStatus().value(),
                "code", errorCode.name(),
                "message", errorCode.message()
        ));

        return Map.of(errorCode.name(), example);
    }

    private void addExamplesToResponses(ApiResponses responses, ExampleHolder exampleHolder) {
        String statusCode = String.valueOf(exampleHolder.getCode());

        ApiResponse existingResponse = responses.get(statusCode);

        if (existingResponse == null) {
            // 응답이 없으면 새로 생성
            existingResponse = new ApiResponse()
                    .description("Error Response")
                    .content(new Content().addMediaType(
                            org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                            new MediaType().examples(exampleHolder.getHolder())
                    ));
            responses.addApiResponse(statusCode, existingResponse);
        } else {
            // 기존 응답이 있으면 examples만 추가
            Content content = existingResponse.getContent();
            if (content != null) {
                MediaType mediaType = content.get(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
                if (mediaType != null) {
                    Map<String, Example> newExamples;
                    if (mediaType.getExamples() != null) {
                        // ✅ 기존 예제를 복사해서 수정 가능하게 만듦
                        newExamples = new HashMap<>(mediaType.getExamples());
                        newExamples.putAll(exampleHolder.getHolder());
                    } else {
                        newExamples = exampleHolder.getHolder();
                    }
                    mediaType.setExamples(newExamples);
                } else {
                    mediaType = new MediaType().examples(exampleHolder.getHolder());
                    content.addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE, mediaType);
                }
            }
        }
    }
}
