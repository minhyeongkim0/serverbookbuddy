package com.example.bookbuddyproject.web.admin;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminLoginForm {

    @NotEmpty(message = "아이디를 입력해주세요")
    private String adminId;

    @NotEmpty(message = "비밀번호를 입력해주세요")
    private String password;
}
