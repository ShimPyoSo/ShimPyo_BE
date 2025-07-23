package com.example.shimpyo.utils;

import com.example.shimpyo.global.BaseException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

import static com.example.shimpyo.global.exceptionType.AuthException.AUTHENTICATION_GET_FAILED;

public class SecurityUtils {

    public static String getLoginId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BaseException(AUTHENTICATION_GET_FAILED);
        }
        return authentication.getName();
    }

    public static long getUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BaseException(AUTHENTICATION_GET_FAILED);
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Map<?,?> principalMap) {
            Object idObj = principalMap.get("userId");
            if(idObj instanceof Number num){
                return num.longValue();
            }
        }
        throw new BaseException(AUTHENTICATION_GET_FAILED);
    }
}
