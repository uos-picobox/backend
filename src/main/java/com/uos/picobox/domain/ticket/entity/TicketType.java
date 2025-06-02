package com.uos.picobox.domain.ticket.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TICKET_TYPE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TICKET_TYPE_ID")
    private Long id;

    @Column(name = "TYPE_NAME", nullable = false, length = 30, unique = true)
    private String typeName;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Builder
    public TicketType(String typeName, String description) {
        this.typeName = typeName;
        this.description = description;
    }

    public void updateDetails(String typeName, String description) {
        this.typeName = typeName;
        this.description = description;
    }
}