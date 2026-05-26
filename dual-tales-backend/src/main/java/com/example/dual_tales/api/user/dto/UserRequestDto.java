package com.example.dual_tales.api.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor //기본생성자 자동 생성
public class UserRequestDto {
    private String email;
    private String nickname;
    private String password;
}
