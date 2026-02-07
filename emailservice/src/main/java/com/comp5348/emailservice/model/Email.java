package com.comp5348.emailservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "emails")
public class Email {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Integer version;

    @Column(nullable = false)
    private String recipientAddress;

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailStatus status = EmailStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime sentAt;

    private String errorMessage;

    private Long orderId;

    public Email(String recipientAddress, String subject, String body, Long orderId) {
        this.recipientAddress = recipientAddress;
        this.subject = subject;
        this.body = body;
        this.orderId = orderId;
    }
}
