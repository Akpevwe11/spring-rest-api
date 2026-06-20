package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dto.CartDto;
import com.codewithmosh.store.dto.CartItemDto;
import com.codewithmosh.store.dto.CartProductDto;
import com.codewithmosh.store.entities.Cart;
import com.codewithmosh.store.entities.CartItem;
import com.codewithmosh.store.entities.Product;
import com.codewithmosh.store.mappers.CartMapper;
import com.codewithmosh.store.repositories.CartRepository;
import com.codewithmosh.store.repositories.ProductRepository;
import com.codewithmosh.store.services.CartService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@Import(CartService.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartRepository cartRepository;

    @MockitoBean
    private ProductRepository productRepository;

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

    @Test
    @DisplayName("POST /carts/{cartId}/items increments quantity when product already exists in cart")
    void addToCart_incrementsExistingItemQuantity() throws Exception {
        UUID cartId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        Long productId = 1L;

        var product = Product.builder()
                .id(productId)
                .name("Laptop")
                .description("Gaming laptop")
                .price(new BigDecimal("1200.00"))
                .build();

        var cart = new Cart();
        cart.setId(cartId);

        var existingItem = new CartItem();
        existingItem.setProduct(product);
        existingItem.setQuantity(2);
        existingItem.setCart(cart);
        cart.getItems().add(existingItem);

        when(cartRepository.getCartWithItems(cartId)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartMapper.toDto(existingItem)).thenReturn(cartItemDto(product, 3));

        mockMvc.perform(post("/carts/{cartId}/items", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 1
                }
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.product.id").value(1))
                .andExpect(jsonPath("$.product.name").value("Laptop"))
                .andExpect(jsonPath("$.quantity").value(3))
                .andExpect(jsonPath("$.totalPrice").value(3600.00));

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(cartCaptor.capture());
        assertThat(cartCaptor.getValue().getItems()).hasSize(1);
        assertThat(existingItem.getQuantity()).isEqualTo(3);
        verify(cartMapper).toDto(existingItem);
    }

    @Test
    @DisplayName("POST /carts/{cartId}/items adds a new product to the cart")
    void addToCart_addsNewItemToCart() throws Exception {
        UUID cartId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        Long productId = 1L;

        var product = Product.builder()
                .id(productId)
                .name("Laptop")
                .description("Gaming laptop")
                .price(new BigDecimal("1200.00"))
                .build();

        var cart = new Cart();
        cart.setId(cartId);

        when(cartRepository.getCartWithItems(cartId)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartMapper.toDto(any(CartItem.class))).thenAnswer(invocation -> {
            CartItem cartItem = invocation.getArgument(0);
            return cartItemDto(cartItem.getProduct(), cartItem.getQuantity());
        });

        mockMvc.perform(post("/carts/{cartId}/items", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.product.id").value(1))
                .andExpect(jsonPath("$.product.name").value("Laptop"))
                .andExpect(jsonPath("$.quantity").value(1))
                .andExpect(jsonPath("$.totalPrice").value(1200.00));

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(cartCaptor.capture());

        var savedCart = cartCaptor.getValue();
        assertThat(savedCart.getItems()).hasSize(1);

        var savedItem = savedCart.getItems().iterator().next();
        assertThat(savedItem.getProduct()).isSameAs(product);
        assertThat(savedItem.getQuantity()).isEqualTo(1);
        assertThat(savedItem.getCart()).isSameAs(cart);
        verify(cartMapper).toDto(savedItem);
    }

    @Test
    @DisplayName("PUT /carts/{cartId}/items/{productId} updates an existing cart item quantity")
    void updateItem_updatesExistingCartItemQuantity() throws Exception {
        UUID cartId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        Long productId = 1L;

        var product = Product.builder()
                .id(productId)
                .name("Laptop")
                .description("Gaming laptop")
                .price(new BigDecimal("1200.00"))
                .build();

        var cart = new Cart();
        cart.setId(cartId);

        var existingItem = new CartItem();
        existingItem.setProduct(product);
        existingItem.setQuantity(2);
        existingItem.setCart(cart);
        cart.getItems().add(existingItem);

        when(cartRepository.getCartWithItems(cartId)).thenReturn(Optional.of(cart));
        when(cartMapper.toDto(existingItem)).thenReturn(cartItemDto(product, 5));

        mockMvc.perform(put("/carts/{cartId}/items/{productId}", cartId, productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "quantity": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product.id").value(1))
                .andExpect(jsonPath("$.product.name").value("Laptop"))
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.totalPrice").value(6000.00));

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(cartCaptor.capture());
        assertThat(cartCaptor.getValue()).isSameAs(cart);
        assertThat(existingItem.getQuantity()).isEqualTo(5);
        verify(cartMapper).toDto(existingItem);
    }

    private CartItemDto cartItemDto(Product product, int quantity) {
        var productDto = new CartProductDto();
        productDto.setId(product.getId());
        productDto.setName(product.getName());
        productDto.setDescription(product.getDescription());
        productDto.setPrice(product.getPrice().doubleValue());

        var cartItemDto = new CartItemDto();
        cartItemDto.setProduct(productDto);
        cartItemDto.setQuantity(quantity);
        cartItemDto.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        return cartItemDto;

    }

}
