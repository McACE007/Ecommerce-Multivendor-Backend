package com.zosh.repository;

import com.zosh.model.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationCodeRepo extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findByEmail(String email);

    Optional<VerificationCode> findByOtp(String otp);
}
