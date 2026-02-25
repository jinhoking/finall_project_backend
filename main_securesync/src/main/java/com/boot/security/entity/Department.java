package com.boot.security.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="Departments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String deptName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @com.fasterxml.jackson.annotation.JsonIgnore // 👈 부모 정보 조회 차단
    private Department parent;

    @OneToMany(mappedBy = "parent")
    @com.fasterxml.jackson.annotation.JsonIgnore // 👈 자식 리스트 조회 차단
    private List<Department> children = new ArrayList<>();

    // 혹시 users 리스트가 있다면 이것도 추가
    @OneToMany(mappedBy = "department")
    @com.fasterxml.jackson.annotation.JsonIgnore // 👈 부서원 정보 조회 차단
    private List<User> users = new ArrayList<>();
}


