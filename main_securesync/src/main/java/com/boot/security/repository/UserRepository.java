package com.boot.security.repository;

import com.boot.security.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 🌟 로그인 ID로 유저 찾기 (컨트롤러에서 사용)
    Optional<User> findByLoginId(String loginId);
    Optional<User> findByName(String name);
    Optional<User> findByEmail(String email);

}