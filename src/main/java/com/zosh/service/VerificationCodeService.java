package com.zosh.service;

import com.zosh.constants.ExceptionMessages;
import com.zosh.exception.InvalidOTPException;
import com.zosh.model.VerificationCode;
import com.zosh.repository.VerificationCodeRepo;
import com.zosh.utils.OtpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import static com.zosh.constants.GlobalConstants.SELLER_PREFIX;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VerificationCodeService {
    private final VerificationCodeRepo verificationCodeRepo;

    public VerificationCode verifyOTP(String otp) {
      VerificationCode verificationCode = verificationCodeRepo.findByOtp(otp).orElseThrow(() -> new InvalidOTPException(ExceptionMessages.INVALID_OTP));
      verificationCodeRepo.delete(verificationCode);
      return verificationCode;
    }

    public String generateOTP(String email) {
        verificationCodeRepo.findByEmail(email).ifPresent(verificationCodeRepo::delete);

        String otp = OtpUtil.generateOtp();
        VerificationCode verificationCode = VerificationCode.builder().email(email).otp(otp).build();
        return verificationCodeRepo.save(verificationCode).getOtp();
    }

    public void verifyOTPWithEmail(String email, String otp) {
        if(email.startsWith(SELLER_PREFIX))
            email = email.substring(SELLER_PREFIX.length());

        Optional<VerificationCode> verificationCode = verificationCodeRepo.findByEmail(email);

        if (verificationCode.isEmpty() || !verificationCode.get().getOtp().equals(otp))
            throw new InvalidOTPException(ExceptionMessages.INVALID_OTP);

        verificationCode.ifPresent(verificationCodeRepo::delete);
    }
}
