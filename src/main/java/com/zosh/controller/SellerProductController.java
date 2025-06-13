package com.zosh.controller;

import com.zosh.exception.ProductNotFoundException;
import com.zosh.exception.SellerException;
import com.zosh.model.Product;
import com.zosh.model.Seller;
import com.zosh.request.CreateProductRequest;
import com.zosh.service.ProductService;
import com.zosh.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.ok(productService.getAllProductsBySellerId(seller.getId()));
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody CreateProductRequest productRequest, Authentication authentication) throws Exception {
        Seller seller = sellerService.getSellerByEmail(authentication.getName());
        return ResponseEntity.ok(productService.createProduct(productRequest, seller));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) throws ProductNotFoundException {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long productId, @RequestBody Product product) throws ProductNotFoundException {
        return ResponseEntity.ok(productService.updateProduct(productId, product));
    }
}
