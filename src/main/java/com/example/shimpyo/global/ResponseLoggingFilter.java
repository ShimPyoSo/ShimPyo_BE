package com.example.shimpyo.global;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ResponseLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // HttpServletResponse를 감싸는 래퍼 생성
        CustomHttpServletResponseWrapper responseWrapper = new CustomHttpServletResponseWrapper((HttpServletResponse) response);

        chain.doFilter(request, responseWrapper);

        // 응답 헤더 출력
        System.out.println("Response Headers:");
        responseWrapper.getHeaderNames().forEach(header ->
                System.out.println(header + ": " + responseWrapper.getHeader(header))
        );

        // 응답 바디 출력
        String responseBody = responseWrapper.getBody();
        System.out.println("Response Body: " + responseBody);

        System.out.println("Response Status: " + ((HttpServletResponse) response).getStatus());

        // 최종 응답 출력
        response.getWriter().write(responseBody);
    }
}
