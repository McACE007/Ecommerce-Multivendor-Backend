package com.zosh.service;

import com.zosh.constants.ExceptionMessages;
import com.zosh.exception.ProductNotFoundException;
import com.zosh.model.Category;
import com.zosh.model.Product;
import com.zosh.model.Seller;
import com.zosh.repository.CategoryRepo;
import com.zosh.repository.ProductRepo;
import com.zosh.request.CreateProductRequest;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;

    public Product createProduct(CreateProductRequest request, Seller seller) {
        Category category1 = categoryRepo.findByCategoryId(request.getCategory());

        if (category1 == null) {
            Category category = new Category();
            category.setCategoryId(request.getCategory());
            category.setLevel(1);
            category1 = categoryRepo.save(category);
        }


        Category category2 = categoryRepo.findByCategoryId(request.getCategory2());

        if (category2 == null) {
            Category category = new Category();
            category.setCategoryId(request.getCategory2());
            category.setLevel(2);
            category.setParentCategory(category1);
            category2 = categoryRepo.save(category);
        }

        Category category3 = categoryRepo.findByCategoryId(request.getCategory3());

        if (category3 == null) {
            Category category = new Category();
            category.setCategoryId(request.getCategory3());
            category.setLevel(3);
            category.setParentCategory(category2);
            category3 = categoryRepo.save(category);
        }

        Integer discountPercentage = calculateDiscountPercentage(request.getMrpPrice(), request.getSellingPrice());

        Product product = new Product();
        product.setSeller(seller);
        product.setCategory(category3);
        product.setDescription(request.getDescription());
        product.setCreatedAt(LocalDateTime.now());
        product.setTitle(request.getTitle());
        product.setColor(request.getColor());
        product.setSellingPrice(request.getSellingPrice());
        product.setMrpPrice(request.getMrpPrice());
        product.setImages(request.getImages());
        product.setSizes(request.getSizes());
        product.setDiscountPercent(discountPercentage);

        return productRepo.save(product);
    }

    private Integer calculateDiscountPercentage(Integer mrpPrice, Integer sellingPrice) {
        if (mrpPrice <= 0)
            throw new IllegalArgumentException("Actual price must be greater than 0");

        double discount = mrpPrice - sellingPrice;
        double discountPercentage = (discount / mrpPrice) * 100;

        return (int) discountPercentage;
    }

    public void deleteProduct(Long productId) throws ProductNotFoundException {
        Product product = findProductById(productId);
        productRepo.delete(product);
    }

    public Product updateProduct(Long productId, Product product) throws ProductNotFoundException {
        findProductById(productId);
        product.setId(productId);
        return productRepo.save(product);
    }

    public Product findProductById(Long productId) throws ProductNotFoundException {
        return productRepo.findById(productId).orElseThrow(() -> {
                    log.error(ExceptionMessages.PRODUCT_NOT_FOUND_DEV, productId);
                    return new ProductNotFoundException(ExceptionMessages.PRODUCT_NOT_FOUND_USER);
                }
        );
    }

    public List<Product> searchProduct(String query) {
        return productRepo.searchProduct(query);
    }

    public Page<Product> getAllProducts(String category, String brand, String color, String sizes, Integer minPrice, Integer maxPrice, Integer minDiscount, String sort, String stock, Integer pageNumber) {
        Specification<Product> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(category)) {
                Join<Product, Category> categoryJoin = root.join("category");
                log.debug("category: {}", category);
                predicates.add(criteriaBuilder.equal(categoryJoin.get("categoryId"), category));
            }

            if (StringUtils.hasText(color)) {
                log.debug("colors: {}", color);
                predicates.add(criteriaBuilder.equal(root.get("color"), color));
            }

            if (StringUtils.hasText(sizes)) {
                log.debug("sizes: {}", sizes);
                predicates.add(criteriaBuilder.equal(root.get("sizes"), sizes));
            }

            if (minPrice != null) {
                log.debug("minPrice: {}", minPrice);
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("sellingPrice"), minPrice));
            }

            if (maxPrice != null) {
                log.debug("maxPrice: {}", maxPrice);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("sellingPrice"), maxPrice));
            }

            if (minDiscount != null) {
                log.debug("minDiscount: {}", minDiscount);
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("discountPercentage"), minDiscount));
            }

            if (stock != null) {
                log.debug("stock: {}", stock);
                predicates.add(criteriaBuilder.equal(root.get("stock"), stock));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable;

        if (StringUtils.hasText(sort))
            pageable = switch (sort) {
                case "price_low" ->
                        PageRequest.of(pageNumber != null ? pageNumber : 0, 10, Sort.by("sellingPrice").ascending());
                case "price_high" ->
                        PageRequest.of(pageNumber != null ? pageNumber : 0, 10, Sort.by("sellingPrice").descending());
                default -> PageRequest.of(pageNumber != null ? pageNumber : 0, 10, Sort.unsorted());
            };
        else
            pageable = PageRequest.of(pageNumber != null ? pageNumber : 0, 10, Sort.unsorted());

        return productRepo.findAll(specification, pageable);
    }

    public List<Product> getAllProductsBySellerId(Long sellerId) {
        return productRepo.findBySellerId(sellerId);
    }
}
