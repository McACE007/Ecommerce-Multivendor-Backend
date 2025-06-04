package com.zosh.controller;

import com.zosh.exception.SellerException;
import com.zosh.model.Product;
import com.zosh.model.Seller;
import com.zosh.request.CreateProductRequest;
import com.zosh.service.ProductService;
import com.zosh.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sellers/products")
public class SellerProductController {
    private final SellerService sellerService;
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProductsBySellerId(Authentication authentication) throws SellerException {
        Seller seller = sellerService.getSellerByEmail(authentication.getName());
        List<Product> products = productService.getAllProductsBySellerId(seller.getId());
        return ResponseEntity.ok(products);
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(CreateProductRequest productRequest, Authentication authentication) throws Exception {
        Seller seller = sellerService.getSellerByEmail(authentication.getName());
        Product newProduct = productService.createProduct(productRequest, seller);
        return ResponseEntity.ok();
    }
}
