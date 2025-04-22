package com.zosh.controller;

import com.zosh.domain.AccountStatus;
import com.zosh.model.Seller;
import com.zosh.model.VerificationCode;
import com.zosh.repository.VerificationCodeRepo;
import com.zosh.service.AuthService;
import com.zosh.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sellers")
public class SellerController {
    private final SellerService sellerService;
    private final AuthService authService;
    private final VerificationCodeRepo verificationCodeRepo;

    @PatchMapping("/verify/{otp}")
    public ResponseEntity<Seller> verifySellerEmail(@PathVariable String otp) throws Exception {
        VerificationCode verificationCode = verificationCodeRepo.findByOtp(otp);

        if (verificationCode == null || !verificationCode.getOtp().equals(otp))
            throw new Exception("Wrong otp");

        Seller seller = sellerService.verifyEmail(verificationCode.getEmail(), otp);

        return ResponseEntity.ok(seller);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Seller> getSellerById(@PathVariable Long id) throws Exception {
        Seller seller = sellerService.getSellerById(id);
        return ResponseEntity.ok(seller);
    }

    @GetMapping("/profile")
    public ResponseEntity<Seller> getSellerByToken(Authentication authentication) throws Exception {
        String email = authentication.getName();
        Seller seller = sellerService.getSellerByEmail(email);
        return ResponseEntity.ok(seller);
    }

    @GetMapping
    public ResponseEntity<List<Seller>> getAllSellers(@RequestParam(required = false) AccountStatus status) {
        List<Seller> sellers = sellerService.getAllSellers(status);
        return ResponseEntity.ok(sellers);
    }

    @PatchMapping
    public ResponseEntity<Seller> updateSeller(Authentication authentication, @RequestBody Seller seller) throws Exception {
        Seller sellerProfile = sellerService.getSellerProfile(authentication);
        Seller updatedSeller = sellerService.updateSeller(sellerProfile.getId(), seller);
        return ResponseEntity.ok(updatedSeller);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSeller(@PathVariable Long id) throws Exception {
        sellerService.deleteSeller(id);
        return ResponseEntity.noContent().build();
    }

    
}
