package com.zosh.controller;

import com.zosh.domain.AccountStatus;
import com.zosh.exception.InvalidOTPException;
import com.zosh.exception.SellerNotFoundException;
import com.zosh.model.Seller;
import com.zosh.model.VerificationCode;
import com.zosh.service.AuthService;
import com.zosh.service.SellerService;
import com.zosh.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sellers")
public class SellerController {
    private final SellerService sellerService;
    private final AuthService authService;
    private final VerificationCodeService verificationCodeService;

    @PatchMapping("/verify/{otp}")
    public ResponseEntity<Seller> verifySellerEmail(@PathVariable String otp) throws InvalidOTPException {
        VerificationCode verificationCode = verificationCodeService.verifyOTP(otp);
        Seller seller = sellerService.verifyEmail(verificationCode.getEmail(), otp);
        return ResponseEntity.ok(seller);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Seller> getSellerById(@PathVariable Long id) throws SellerNotFoundException {
        Seller seller = sellerService.getSellerById(id);
        return ResponseEntity.ok(seller);
    }

    @GetMapping("/profile")
    public ResponseEntity<Seller> getSellerByToken(Authentication authentication) throws SellerNotFoundException {
        return ResponseEntity.ok(sellerService.getSellerByEmail(authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<Seller>> getAllSellers(@RequestParam(required = false) AccountStatus status) {
        List<Seller> sellers = sellerService.getAllSellers(status);
        return ResponseEntity.ok(sellers);
    }

    @PatchMapping
    public ResponseEntity<Seller> updateSeller(Authentication authentication, @RequestBody Seller seller) throws SellerNotFoundException {
        Seller sellerProfile = sellerService.getSellerByEmail(authentication.getName());
        Seller updatedSeller = sellerService.updateSeller(sellerProfile.getId(), seller);
        return ResponseEntity.ok(updatedSeller);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSeller(@PathVariable Long id) throws SellerNotFoundException {
        sellerService.deleteSeller(id);
        return ResponseEntity.noContent().build();
    }
}
