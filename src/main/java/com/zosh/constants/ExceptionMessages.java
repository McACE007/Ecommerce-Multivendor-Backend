package com.zosh.constants;

public class ExceptionMessages {
    public final static String SELLER_ALREADY_EXISTS_DEV = "Seller creation failed: email '{}' already exists";
    public final static String SELLER_ALREADY_EXISTS_USER = "A seller with this email already exists. Please use a different email";
    public final static String SELLER_NOT_FOUND_ID_DEV = "Seller lookup failed: sellerId '{}' does not exist";
    public final static String SELLER_NOT_FOUND_EMAIL_DEV = "Seller lookup failed: email '{}' does not exist";
    public final static String SELLER_NOT_FOUND_USER = "Seller not found";
    public final static String INVALID_OTP = "The OTP you entered is incorrect. Please try again.";
    public final static String PRODUCT_NOT_FOUND_USER = "Product not found";
    public final static String PRODUCT_NOT_FOUND_DEV = "Product lookup failed: productId '{}' does not exist";
}
