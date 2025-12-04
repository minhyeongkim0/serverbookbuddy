package com.example.bookbuddyproject.web;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberForm {

    @NotEmpty(message = "아이디는 필수입니다")
    @Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "아이디는 4-20자의 영문, 숫자 조합이어야 합니다")
    private String loginId;

    @NotEmpty(message = "비밀번호는 필수입니다")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).{8,20}$", 
             message = "비밀번호는 8-20자의 영문, 숫자, 특수문자 조합이어야 합니다")
    private String password;

    @NotEmpty(message = "이메일은 필수입니다")
    @Pattern(regexp = "^[a-zA-Z0-9]+@(yu\\.ac\\.kr|ynu\\.ac\\.kr)$", message = "영남대학교 메일로만 인증 가능합니다. (@yu.ac.kr 또는 @ynu.ac.kr)")
    private String email;

    @NotEmpty(message = "학과는 필수입니다")
    private String department;

    @NotEmpty(message = "학번은 필수입니다")
    private String studentId;

    private String emailVerified;  // 이메일 인증 여부
}
