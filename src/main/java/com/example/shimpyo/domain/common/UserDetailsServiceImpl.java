package com.example.shimpyo.domain.common;

import com.example.shimpyo.domain.auth.repository.UserAuthRepository;
import com.example.shimpyo.global.BaseException;
import com.example.shimpyo.global.exceptionType.MemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserAuthRepository userAuthRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new UserDetailsImpl(userAuthRepository.findByUserLoginId(username)
                .orElseThrow(() -> new UsernameNotFoundException("회원이 존재하지 않습니다.")));
    }

    public UserDetailsImpl loadUserByUserLoginId(String username) throws UsernameNotFoundException {
        return new UserDetailsImpl(userAuthRepository.findByUserLoginId(username)
                .orElseThrow(() -> new UsernameNotFoundException("회원이 존재하지 않습니다.")));
    }
}
