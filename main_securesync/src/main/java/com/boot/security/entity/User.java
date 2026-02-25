package com.boot.security.entity;


import com.boot.security.config.DataEncryptor;
import com.boot.security.role.UserRole;
import com.boot.security.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String loginId;

    @Column(nullable = false)
    private String password;

    //보안 컬럼
    @Convert(converter = DataEncryptor.class)
    @Column(name="name_enc")
    private String name;

    @Convert(converter = DataEncryptor.class)
    @Column(name="email_enc")
    private String email;

    @Convert(converter = DataEncryptor.class)
    @Column(name ="phone_enc")
    private String phone;

    @Convert(converter = DataEncryptor.class)
    @Column(name="address_enc")
    private String address;

    @Column(name="position")
    //@Convert(converter = DataEncryptor.class)
    private String position; // 사원, 대리, 과장, 팀장 등
    ////////////////////////

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    @Column(name="hire_date")
    @com.fasterxml.jackson.annotation.JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("joinDate") // 🌟 JSON으로 나갈 때 이름을 "joinDate"로 강제 고정
    private LocalDateTime joinDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"parent", "children", "users", "hibernateLazyInitializer", "handler"}) // 👈 부서의 부모/자식 정보는 가져오지 않음
    private Department department;

    @OneToMany(mappedBy = "writer")
    @com.fasterxml.jackson.annotation.JsonIgnore // 👈 이 어노테이션을 추가해서 순환 참조를 원천 차단하세요.
    private List<Document> documents = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        // 🌟 가입할 때 날짜가 안 넘어오면 현재 시간으로 자동 세팅 (이게 핵심!)
        if(this.joinDate == null) {
            this.joinDate = LocalDateTime.now();
        }
    }
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

}
