package com.zosh.service;

import com.zosh.constants.ExceptionMessages;
import com.zosh.exception.InvalidOTPException;
import com.zosh.model.VerificationCode;
import com.zosh.repository.VerificationCodeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerificationCodeService {
    private final VerificationCodeRepo verificationCodeRepo;

    public VerificationCode verifyOTP(String otp) {
        return verificationCodeRepo.findByOtp(otp).orElseThrow(() -> new InvalidOTPException(ExceptionMessages.INVALID_OTP));
    }
}
