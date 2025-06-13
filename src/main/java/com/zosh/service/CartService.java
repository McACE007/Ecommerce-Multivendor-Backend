package com.zosh.service;

import com.zosh.exception.CartNotFoundException;
import com.zosh.model.Cart;
import com.zosh.model.CartItem;
import com.zosh.model.Product;
import com.zosh.model.User;
import com.zosh.repository.CartItemRepo;
import com.zosh.repository.CartRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import static com.zosh.constants.ExceptionMessages.CART_NOT_FOUND_USERID_DEV;
import static com.zosh.constants.ExceptionMessages.CART_NOT_FOUND_USER;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepo cartRepo;
    private final CartItemRepo cartItemRepo;

    public CartItem addCartItem(User user, Product product, String size, int quantity){
        Cart cart = findUserCart(user);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setSize(size);
        cartItem.setQuantity(quantity);
        cartItem.setSellingPrice(product.getSellingPrice());
        cartItem.setMrpPrice(product.getMrpPrice());

        CartItem savedCartItem = cartItemRepo.save(cartItem);

        cart.getCartItems().add(savedCartItem);
        cart.setCartItems(cart.getCartItems());

        Cart savedCart = cartRepo.save(cart);

        return savedCartItem;
    }

    public Cart findUserCart(User user) throws CartNotFoundException{
        Cart cart = cartRepo.findByUserId(user.getId()).orElseThrow(() -> {
            log.error(CART_NOT_FOUND_USERID_DEV, user.getId());
            return new CartNotFoundException(CART_NOT_FOUND_USER);
        });

        double totalSellingPrice = 0;
        double totalMrpPrice = 0;
        int totalItems = 0;

        for(CartItem cartItem : cart.getCartItems()){
            totalMrpPrice += cartItem.getMrpPrice();
            totalItems += cartItem.getQuantity();
            totalSellingPrice += cartItem.getSellingPrice();
        }

        cart.setTotalMrpPrice(totalMrpPrice);
        cart.setTotalSellingPrice(totalSellingPrice);
        cart.setTotalItems(totalItems);
        cart.setDiscount(calculateDiscountPercentage(totalMrpPrice, totalSellingPrice));

        return cart;
    }

    private Integer calculateDiscountPercentage(double mrpPrice, double sellingPrice) {
        if (mrpPrice <= 0)
            throw new IllegalArgumentException("Actual price must be greater than 0");

        double discount = mrpPrice - sellingPrice;
        double discountPercentage = (discount / mrpPrice) * 100;

        return (int) discountPercentage;
    }
}
