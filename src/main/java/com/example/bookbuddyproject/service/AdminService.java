package com.example.bookbuddyproject.service;

import com.example.bookbuddyproject.domain.Admin;
import com.example.bookbuddyproject.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;

    /**
     * 관리자 로그인
     */
    public Admin login(String adminId, String password) {
        Admin admin = adminRepository.findByAdminId(adminId)
                .orElseThrow(() -> new IllegalStateException("아이디 또는 비밀번호가 일치하지 않습니다."));

        if (!admin.getPassword().equals(password)) {
            throw new IllegalStateException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        return admin;
    }

    /**
     * 관리자 추가
     */
    @Transactional
    public Long addAdmin(String adminId, String password, String masterPassword) {
        // 마스터 비밀번호 확인
        Admin master = adminRepository.findMaster()
                .orElseThrow(() -> new IllegalStateException("마스터 관리자가 존재하지 않습니다."));

        if (!master.getPassword().equals(masterPassword)) {
            throw new IllegalStateException("마스터 비밀번호가 일치하지 않습니다.");
        }

        // 아이디 중복 확인
        if (adminRepository.existsByAdminId(adminId)) {
            throw new IllegalStateException("이미 존재하는 관리자 아이디입니다.");
        }

        // 관리자 생성 (일반 관리자)
        Admin admin = Admin.createAdmin(adminId, password, false);
        adminRepository.save(admin);

        return admin.getId();
    }

    /**
     * 관리자 목록 조회
     */
    public List<Admin> findAll() {
        return adminRepository.findAll();
    }
}
