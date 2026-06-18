package com.codewithmosh.store.controllers;
import com.codewithmosh.store.dto.AddItemToCartRequest;
import com.codewithmosh.store.dto.CartDto;
import com.codewithmosh.store.dto.CartItemDto;
import com.codewithmosh.store.dto.UpdateCartItemRequest;
import com.codewithmosh.store.entities.Cart;
import com.codewithmosh.store.entities.CartItem;
import com.codewithmosh.store.mappers.CartMapper;
import com.codewithmosh.store.repositories.CartRepository;
import com.codewithmosh.store.repositories.ProductRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/carts")
public class CartController {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final ProductRepository productRepository;



    @PostMapping
    public ResponseEntity<CartDto> createCart(
            UriComponentsBuilder uriBuilder
    ){

     Cart cartItem = cartRepository.save(new Cart());
     CartDto cartDto =  cartMapper.toDto(cartItem);

     var uri = uriBuilder.path("/carts/{id}").buildAndExpand(cartItem.getId()).toUri();
     return ResponseEntity.created(uri).body(cartDto);


    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartItemDto> addToCart(@PathVariable UUID cartId,
                                                 @RequestBody AddItemToCartRequest request) {
        var cart = cartRepository.getCartWithItems(cartId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

        var product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found"));

        var cartItem = cart.addItem(product);

        cartRepository.save(cart);

        var cartItemDto = cartMapper.toDto(cartItem);

        return ResponseEntity.status(HttpStatus.CREATED).body(cartItemDto);

    }

    @GetMapping("/{cartId}")
    public ResponseEntity<CartDto> getCart(@PathVariable UUID cartId) {

        var cart = cartRepository.getCartWithItems(cartId).orElse(null);

        if (cart == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(cartMapper.toDto(cart));
    }

    @PutMapping("/{cartId}/items/{productId}")
    public ResponseEntity<?> updateItem(
            @PathVariable("cartId") UUID cartId,
            @PathVariable("productId") Long productId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {

        var cart = cartRepository.getCartWithItems(cartId).orElse(null);
        if (cart == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("error", "Cart not found.")
            );
        }

        var cartItem = cart.getItem(productId);

        if (cartItem == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("error", "Item not found in cart.")
            );
        }

        cartItem.setQuantity(request.getQuantity());
        cartRepository.save(cart);
        return ResponseEntity.ok(
                cartMapper.toDto(cartItem)
        );

    }
}
