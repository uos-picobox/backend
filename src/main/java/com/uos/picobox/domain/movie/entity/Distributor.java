package com.uos.picobox.domain.movie.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "DISTRIBUTOR")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Distributor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DISTRIBUTOR_ID")
    private Long id;

    @Column(name = "NAME", nullable = false, length = 50)
    private String name;

    @Column(name = "ADDRESS", length = 300)
    private String address;

    @Column(name = "PHONE", length = 20)
    private String phone;

    @Builder
    public Distributor(String name, String address, String phone) {
        this.name = name;
        this.address = address;
        this.phone = phone;
    }

    public void updateDetails(String name, String address, String phone) {
        this.name = name;
        this.address = address;
        this.phone = phone;
    }
}