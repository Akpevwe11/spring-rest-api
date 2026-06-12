package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dto.CartDto;
import com.codewithmosh.store.entities.Cart;
import com.codewithmosh.store.mappers.CartMapper;
import com.codewithmosh.store.repositories.CartRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@AllArgsConstructor
@RestController
@RequestMapping("/carts")
public class CartController {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;



    @PostMapping
    public ResponseEntity<CartDto> createCart(
            UriComponentsBuilder uriBuilder
    ){

     Cart cartItem = cartRepository.save(new Cart());
     CartDto cartDto =  cartMapper.toDto(cartItem);

     var uri = uriBuilder.path("/carts/{id}").buildAndExpand(cartItem.getId()).toUri();
     return ResponseEntity.created(uri).body(cartDto);


    }
}
