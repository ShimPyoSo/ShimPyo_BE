package com.example.shimpyo.domain.user.service;

import com.example.shimpyo.domain.user.repository.UserRepository;
import com.example.shimpyo.global.BaseException;
import com.example.shimpyo.global.exceptionType.MemberExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public void changeNickname(String email, String nickname) {
        userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new BaseException(MemberExceptionType.MEMBER_NOT_FOUND))
                .changeNickname(nickname);
    }

    public boolean checkNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
}
