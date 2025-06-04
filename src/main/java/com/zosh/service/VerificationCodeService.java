package com.zosh.service;

import com.zosh.constants.ExceptionMessages;
import com.zosh.exception.InvalidOTPException;
import com.zosh.model.VerificationCode;
import com.zosh.repository.VerificationCodeRepo;
import com.zosh.utils.OtpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerificationCodeService {
    private final VerificationCodeRepo verificationCodeRepo;

    public VerificationCode verifyOTP(String otp) {
        return verificationCodeRepo.findByOtp(otp).orElseThrow(() -> new InvalidOTPException(ExceptionMessages.INVALID_OTP));
    }

    public String generateOTP(String email) {
        verificationCodeRepo.findByEmail(email).ifPresent(verificationCodeRepo::delete);

        String otp = OtpUtil.generateOtp();
        VerificationCode verificationCode = VerificationCode.builder().email(email).otp(otp).build();
        return verificationCodeRepo.save(verificationCode).getOtp();
    }

    public void verifyOTPWithEmail(String email) {
        VerificationCode verificationCode = verificationCodeRepo.findByEmail(email).orElseThrow(() -> new InvalidOTPException(ExceptionMessages.INVALID_OTP));
        verificationCodeRepo.delete(verificationCode);
    }
}
