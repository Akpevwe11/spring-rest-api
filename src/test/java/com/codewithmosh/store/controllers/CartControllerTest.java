package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dto.CartDto;
import com.codewithmosh.store.entities.Cart;
import com.codewithmosh.store.mappers.CartMapper;
import com.codewithmosh.store.repositories.CartRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartRepository cartRepository;

    @MockitoBean
    private CartMapper cartMapper;

    @Test
    @DisplayName("POST /carts creates a cart and returns 201")
    void createCart_returnsCreatedCart() throws Exception {
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");

        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart cart = invocation.getArgument(0);
            cart.setId(id);
            return cart;
        });

        CartDto dto = new CartDto();
        dto.setId(id); // change to dto.setId(id) if CartDto.id is UUID
        when(cartMapper.toDto(any(Cart.class))).thenReturn(dto);

        mockMvc.perform(post("/carts"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/carts/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()));

        ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(captor.capture());
        verify(cartMapper).toDto(any(Cart.class));
    }
}