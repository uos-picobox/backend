package com.uos.picobox.admin.entity;

import com.uos.picobox.global.converter.AdminRoleConverter;
import com.uos.picobox.global.converter.BooleanToYNConverter;
import com.uos.picobox.global.enumClass.AdminRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ADMIN")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ADMIN_ID")
    private Long id;

    @Column(name = "LOGIN_ID", nullable = false, unique = true, length = 50)
    @NotBlank
    private String loginId;

    @Column(name = "PASSWORD", nullable = false, length = 128)
    @NotBlank
    private String password;

    @Column(name = "NAME", nullable = false, length = 50)
    @NotBlank
    private String name;

    @Column(name = "EMAIL", nullable = false, unique = true, length = 100)
    @Email
    @NotBlank
    private String email;

    @Column(name = "REGISTERED_AT", nullable = false)
    private LocalDateTime registeredAt;

    @Column(name = "LAST_LOGIN_AT")
    private LocalDateTime lastLoginAt;

    @Convert(converter = AdminRoleConverter.class)
    @Column(name = "ROLE", nullable = false, length = 20)
    @NotNull
    private AdminRole role;

    @Column(name = "IS_ACTIVE", nullable = false, length = 1)
    @Convert(converter = BooleanToYNConverter.class)
    private Boolean isActive;

    @Builder
    public Admin(String loginId, String password, String name, String email, AdminRole role) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.email = email;
        this.registeredAt = LocalDateTime.now();
        this.lastLoginAt = LocalDateTime.now();
        this.role = role;
        this.isActive = true;
    }
}