package com.example.shimpyo.domain.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

    private static final String ACCESS_COOKIE = "access_token";
    private static final String REFRESH_COOKIE = "refresh_token";

    public String getCookieValue(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) {
            if (REFRESH_COOKIE.equals(c.getName())) return c.getValue();
        }
        return null;
    }

    public ResponseCookie buildRefreshCookie(String value, long maxAgeSeconds) {
        return ResponseCookie.from(REFRESH_COOKIE, value)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("None")
                .build();
    }

    public ResponseCookie buildAccessCookie(String value, long maxAgeSeconds) {
        return ResponseCookie.from(ACCESS_COOKIE, value)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("None")
                .build();
    }

    public void addCookies(HttpServletResponse response, ResponseCookie... cookies) {
        for (ResponseCookie c : cookies) {
            response.addHeader("Set-Cookie", c.toString());
        }
    }

    public void clearAuthCookies(HttpServletResponse response) {
        addCookies(response,
                buildAccessCookie("", 0),
                buildRefreshCookie("", 0)
        );
    }
}
