package com.example.shimpyo.domain.user.service;

import com.example.shimpyo.domain.user.repository.UserRepository;
import com.example.shimpyo.global.BaseException;
import com.example.shimpyo.global.exceptionType.MemberExceptionType;
import com.example.shimpyo.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional()
    public void changeNickname(String nickname) {
        userRepository.findById(SecurityUtils.getUserId())
                .orElseThrow(() -> new BaseException(MemberExceptionType.MEMBER_NOT_FOUND))
                .changeNickname(nickname);
    }

    public void checkNickname(String nickname) {
        if (userRepository.existsByNickname(nickname))
            throw new BaseException(MemberExceptionType.NICKNAME_DUPLICATED);
    }
}
