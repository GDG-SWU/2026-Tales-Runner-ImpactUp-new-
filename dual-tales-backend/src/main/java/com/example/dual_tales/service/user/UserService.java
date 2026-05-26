package com.example.dual_tales.service.user;

import com.example.dual_tales.api.user.dto.UserRequestDto;
import com.example.dual_tales.api.user.dto.UserResponseDto;
import com.example.dual_tales.domain.user.User;
import com.example.dual_tales.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder PasswordEncoder;
    private final PasswordEncoder passwordEncoder;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public Long join(UserRequestDto requestDto) {
        //이메일 중복 체크
        if(userRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        //비밀번호 암호화 후 저장
        User user = new User();
        user.setEmail(requestDto.getEmail());
        user.setNickname(requestDto.getNickname());
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        user.setRole("USER");

        return userRepository.save(user).getId();
    }

    @Transactional(readOnly = true)
    public UserResponseDto login(String email, String password) {
        //이메일로 유저 찾기
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        //비밀번호 비교
        //password : 로그인 시 입력한 비밀번호
        //user.getPassword() : DB에 저장된 암호화된 비밀번호
        if(!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        //로그인 성공시 유저 ID 반환
        return new UserResponseDto(user);
    }
}
