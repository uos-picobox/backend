package com.uos.picobox.user.entity;

import com.uos.picobox.global.converter.BooleanToYNConverter;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "CUSTOMER")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CUSTOMER_ID")
    private Long id;

    @Column(name = "LOGIN_ID", nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(name = "PASSWORD", nullable = false, length = 128)
    private String password;

    @Column(name = "NAME", nullable = false, length = 50)
    private String name;

    @Column(name = "EMAIL", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "PHONE", nullable = false, length = 20)
    private String phone;

    @Column(name = "DATE_OF_BIRTH")
    private LocalDate dateOfBirth;

    @Column(name = "GENDER", length = 10)
    private String gender;

    @Column(name = "POINTS", nullable = false)
    private Integer points;

    @Column(name = "REGISTERED_AT", nullable = false)
    private LocalDateTime registeredAt;

    @Column(name = "LAST_LOGIN_AT")
    private LocalDateTime lastLoginAt;

    @Column(name = "IS_ACTIVE", nullable = false, length = 1)
    @Convert(converter = BooleanToYNConverter.class)
    private Boolean isActive;

    @Builder
    public Customer(String loginId, String password, String name, String email, String phone, LocalDate dateOfBirth, String gender) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.points = 0;
        this.registeredAt = LocalDateTime.now();
        this.lastLoginAt = LocalDateTime.now();
        this.isActive = true;
    }
}