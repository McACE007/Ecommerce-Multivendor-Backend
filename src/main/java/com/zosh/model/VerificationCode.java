package com.zosh.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class VerificationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String otp;

    private String email;

    @OneToOne
    private User user;

    @OneToOne
    private Seller seller;
}
