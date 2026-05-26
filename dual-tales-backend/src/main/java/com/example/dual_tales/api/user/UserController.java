package com.example.dual_tales.api.user;

import com.example.dual_tales.api.user.dto.LoginRequestDto;
import com.example.dual_tales.api.user.dto.UserRequestDto;
import com.example.dual_tales.api.user.dto.UserResponseDto;
import com.example.dual_tales.domain.user.User;
import com.example.dual_tales.global.security.JwtTokenProvider;
import com.example.dual_tales.service.user.UserService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "유저 관련 API")
public class UserController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임을 받아 회원가입을 진행합니다.")
    public ResponseEntity<Long> signUp(@RequestBody UserRequestDto requestDto) {
        //UserService의 join 메서드 호출하여 회원가입
        return ResponseEntity.ok(userService.join(requestDto));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인을 시도합니다.")
    public ResponseEntity<UserResponseDto> login(@RequestBody LoginRequestDto loginDto) {
        //UserService의 login 메서드 호출하여 비밀번호 검증 진행
        UserResponseDto response = userService.login(loginDto.getEmail(), loginDto.getPassword());
        //가져온 정보에서 id를 뽑아 토큰 생성
        String token = jwtTokenProvider.createToken(response.getId().toString());
        //Setter로 토큰을 넣어줌
        response.setAccessToken(token);

        return ResponseEntity.ok(response);
    }
}
