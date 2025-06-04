package com.zosh.service;

import com.zosh.domain.AccountStatus;
import com.zosh.domain.USER_ROLE;
import com.zosh.exception.ExceptionMessages;
import com.zosh.exception.SellerAlreadyExistsException;
import com.zosh.exception.SellerNotFoundException;
import com.zosh.model.Address;
import com.zosh.model.Seller;
import com.zosh.repository.AddressRepo;
import com.zosh.repository.SellerRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerService {
    private final SellerRepo sellerRepo;
    private final PasswordEncoder passwordEncoder;
    private final AddressRepo addressRepo;

    public Seller createSeller(Seller seller) throws SellerAlreadyExistsException {
        log.info("sellerService createdSeller() started");

        Optional<Seller> existingSeller = sellerRepo.findByEmail(seller.getEmail());

        if (existingSeller.isPresent()) {
            log.error(ExceptionMessages.SELLER_ALREADY_EXISTS_DEV, seller.getEmail());
            throw new SellerAlreadyExistsException(ExceptionMessages.SELLER_ALREADY_EXISTS_USER);
        }

        Address savedAddress = addressRepo.save(seller.getPickupAddress());
        seller.setPassword(passwordEncoder.encode(seller.getPassword()));
        seller.setPickupAddress(savedAddress);
        seller.setRole(USER_ROLE.ROLE_SELLER);

        log.info("sellerService createdSeller() ended");
        return sellerRepo.save(seller);
    }

    public Seller getSellerById(Long id) throws SellerNotFoundException {
        log.info("sellerService getSellerById() started");

        Optional<Seller> seller = sellerRepo.findById(id);
        if (seller.isEmpty()) {
            log.error(ExceptionMessages.SELLER_NOT_FOUND_ID_DEV);
            throw new SellerNotFoundException(ExceptionMessages.SELLER_NOT_FOUND_USER);
        }

        log.info("sellerService getSellerById() ended");

        return seller.get();
    }

    public Seller getSellerByEmail(String email) throws SellerNotFoundException {
        log.info("sellerService getSellerByEmail() started");

        Optional<Seller> seller = sellerRepo.findByEmail(email);
        if (seller.isEmpty()) {
            log.error(ExceptionMessages.SELLER_NOT_FOUND_EMAIL_DEV);
            throw new SellerNotFoundException(ExceptionMessages.SELLER_NOT_FOUND_USER);
        }

        log.info("sellerService getSellerByEmail() ended");

        return seller.get();
    }

    public List<Seller> getAllSellers(AccountStatus status) {
        log.info("sellerService getAllSellers() called");
        return sellerRepo.findByAccountStatus(status);
    }

    public Seller updateSeller(Long id, Seller seller) throws SellerNotFoundException {
        log.info("sellerService updateSeller() started");

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

        log.info("sellerService updateSeller() ended");

        return sellerRepo.save(existingSeller);
    }

    public void deleteSeller(Long id) throws SellerNotFoundException {
        log.info("sellerService deleteSeller() started");

        Seller seller = getSellerById(id);
        sellerRepo.delete(seller);

        log.info("sellerService deleteSeller() ended");
    }

    public Seller verifyEmail(String email, String otp) throws SellerNotFoundException {
        log.info("sellerService verifyEmail() started");

        Seller seller = getSellerByEmail(email);
        seller.setEmailVerified(true);

        log.info("sellerService verifyEmail() ended");

        return sellerRepo.save(seller);
    }

    public Seller updateSellerAccountStatus(Long sellerId, AccountStatus status) throws SellerNotFoundException {
        log.info("sellerService updateSellerAccountStatus() started");

        Seller seller = getSellerById(sellerId);
        seller.setAccountStatus(status);

        log.info("sellerService updateSellerAccountStatus() ended");

        return sellerRepo.save(seller);
    }
}
