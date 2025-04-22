package com.zosh.service;

import com.zosh.domain.AccountStatus;
import com.zosh.domain.USER_ROLE;
import com.zosh.model.Address;
import com.zosh.model.Seller;
import com.zosh.repository.AddressRepo;
import com.zosh.repository.SellerRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SellerService {
    private final SellerRepo sellerRepo;
    private final PasswordEncoder passwordEncoder;
    private final AddressRepo addressRepo;

    public Seller getSellerProfile(Authentication authentication) throws Exception {
        String email = authentication.getName();
        return getSellerByEmail(email);
    }

    public Seller createSeller(Seller seller) throws Exception {
        Seller sellerExist = sellerRepo.findByEmail(seller.getEmail());

        if (sellerExist != null)
            throw new Exception("seller already exist, use different email");

        Address savedAddress = addressRepo.save(seller.getPickupAddress());
        seller.setPassword(passwordEncoder.encode(seller.getPassword()));
        seller.setPickupAddress(savedAddress);
        seller.setRole(USER_ROLE.ROLE_SELLER);

        return sellerRepo.save(seller);
    }

    public Seller getSellerById(Long id) throws Exception {
        Optional<Seller> seller = sellerRepo.findById(id);
        if (seller.isEmpty()) throw new Exception("seller not exists with that seller id");
        return seller.get();
    }

    public Seller getSellerByEmail(String email) throws Exception {
        Seller seller = sellerRepo.findByEmail(email);
        if (seller == null) throw new Exception("seller not exists with that email");
        return seller;
    }

    public List<Seller> getAllSellers(AccountStatus status) {
        return sellerRepo.findByAccountStatus(status);
    }

    public Seller updateSeller(Long id, Seller seller) throws Exception {
        Seller existingSeller = getSellerById(id);

        if (StringUtils.hasText(seller.getSellerName()))
            existingSeller.setSellerName(seller.getSellerName());
        if (StringUtils.hasText(seller.getMobile()))
            existingSeller.setMobile((seller.getMobile()));
        if (StringUtils.hasText(seller.getEmail()))
            existingSeller.setEmail(seller.getEmail());
        if (seller.getBusinessDetails() != null
                && StringUtils.hasText(seller.getBusinessDetails().getBusinessName())
                && StringUtils.hasText(seller.getBusinessDetails().getBusinessEmail())
                && StringUtils.hasText(seller.getBusinessDetails().getBusinessMobile())
                && StringUtils.hasText(seller.getBusinessDetails().getBusinessAddress())
                && StringUtils.hasText(seller.getBusinessDetails().getLogo())
                && StringUtils.hasText(seller.getBusinessDetails().getBanner())
        ) {
            existingSeller.getBusinessDetails().setBusinessName(seller.getBusinessDetails().getBusinessName());
            existingSeller.getBusinessDetails().setBusinessEmail(seller.getBusinessDetails().getBusinessEmail());
            existingSeller.getBusinessDetails().setBusinessMobile(seller.getBusinessDetails().getBusinessMobile());
            existingSeller.getBusinessDetails().setBusinessAddress(seller.getBusinessDetails().getBusinessAddress());
            existingSeller.getBusinessDetails().setLogo(seller.getBusinessDetails().getLogo());
            existingSeller.getBusinessDetails().setBanner(seller.getBusinessDetails().getBanner());
        }
        if (seller.getBankDetails() != null && StringUtils.hasText(seller.getBankDetails().getAccountNumber()) && StringUtils.hasText(seller.getBankDetails().getAccountHolderName()) && StringUtils.hasText(seller.getBankDetails().getIfscCode())) {
            existingSeller.getBankDetails().setAccountNumber(seller.getBankDetails().getAccountNumber());
            existingSeller.getBankDetails().setAccountHolderName(seller.getBankDetails().getAccountHolderName());
            existingSeller.getBankDetails().setIfscCode(seller.getBankDetails().getIfscCode());
        }
        if (seller.getPickupAddress() != null
                && StringUtils.hasText(seller.getPickupAddress().getAddress())
                && StringUtils.hasText(seller.getPickupAddress().getMobile())
                && StringUtils.hasText(seller.getPickupAddress().getName())
                && StringUtils.hasText(seller.getPickupAddress().getCity())
                && StringUtils.hasText(seller.getPickupAddress().getLocality())
                && StringUtils.hasText(seller.getPickupAddress().getState())
                && StringUtils.hasText(seller.getPickupAddress().getPinCode())
        ) {
            existingSeller.getPickupAddress().setAddress(seller.getPickupAddress().getAddress());
            existingSeller.getPickupAddress().setMobile(seller.getPickupAddress().getMobile());
            existingSeller.getPickupAddress().setName(seller.getPickupAddress().getName());
            existingSeller.getPickupAddress().setCity(seller.getPickupAddress().getCity());
            existingSeller.getPickupAddress().setLocality(seller.getPickupAddress().getLocality());
            existingSeller.getPickupAddress().setState(seller.getPickupAddress().getState());
            existingSeller.getPickupAddress().setPinCode(seller.getPickupAddress().getPinCode());
        }
        if (StringUtils.hasText(seller.getGSTIN()))
            existingSeller.setGSTIN(seller.getGSTIN());

        return sellerRepo.save(existingSeller);
    }

    public void deleteSeller(Long id) throws Exception {
        Seller seller = getSellerById(id);
        sellerRepo.delete(seller);
    }

    public Seller verifyEmail(String email, String otp) throws Exception {
        Seller seller = getSellerByEmail(email);
        seller.setEmailVerified(true);
        return sellerRepo.save(seller);
    }

    public Seller updateSellerAccountStatus(Long sellerId, AccountStatus status) throws Exception {
        Seller seller = getSellerById(sellerId);
        seller.setAccountStatus(status);
        return sellerRepo.save(seller);
    }
}
