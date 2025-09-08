package com.example.shimpyo.domain.utils;

import com.example.shimpyo.domain.common.UserDetailsImpl;
import com.example.shimpyo.global.BaseException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static com.example.shimpyo.global.exceptionType.AuthException.AUTHENTICATION_GET_FAILED;

public class SecurityUtils {

    public static String getLoginId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BaseException(AUTHENTICATION_GET_FAILED);
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            return userDetails.getUsername();
        }

        return null; // 혹은 비회원 처리
    }

    public static long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BaseException(AUTHENTICATION_GET_FAILED);
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            return userDetails.getUserAuth().getUser().getId();
        }

        throw new BaseException(AUTHENTICATION_GET_FAILED);
    }
}
