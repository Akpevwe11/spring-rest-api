package com.codewithmosh.store.services;

import com.codewithmosh.store.dto.CheckoutRequest;
import com.codewithmosh.store.dto.CheckoutResponse;
import com.codewithmosh.store.dto.ErrorDto;
import com.codewithmosh.store.entities.Order;
import com.codewithmosh.store.exceptions.CartEmptyException;
import com.codewithmosh.store.exceptions.CartNotFoundException;
import com.codewithmosh.store.repositories.CartRepository;
import com.codewithmosh.store.repositories.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class CheckoutService {
    private final CartService cartService;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final AuthService authService;

    public CheckoutResponse checkout(CheckoutRequest request) {

        var cart = cartRepository.getCartWithItems(request.getCartId()).orElse(null);
        if (cart == null) {
           throw new CartNotFoundException();
        }

        if (cart.isEmpty()) {
            throw new CartEmptyException("Cart is empty. Cannot proceed to checkout.");
        }

        var order = Order.fromCart(cart, authService.getCurrentUser());

        orderRepository.save(order);
        cartService.clearCart(cart.getId());

        return new CheckoutResponse(order.getId());

    }
}
