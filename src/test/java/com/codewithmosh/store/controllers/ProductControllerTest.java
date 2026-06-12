package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dto.ProductDto;
import com.codewithmosh.store.entities.Product;
import com.codewithmosh.store.mappers.ProductMapper;
import com.codewithmosh.store.repositories.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private ProductMapper productMapper;

    @Test
    @DisplayName("GET /products returns all products when categoryId is not provided")
    void getAllProducts_withoutCategoryId_returnsAllProducts() throws Exception {
        Product product1 = Product.builder()
                .id(1L)
                .name("Laptop")
                .description("Gaming laptop")
                .price(new BigDecimal("1200.00"))
                .build();

        Product product2 = Product.builder()
                .id(2L)
                .name("Mouse")
                .description("Wireless mouse")
                .price(new BigDecimal("25.50"))
                .build();

        ProductDto dto1 = ProductDto.builder()
                .id(1L)
                .name("Laptop")
                .description("Gaming laptop")
                .price(new BigDecimal("1200.00"))
                .categoryId((byte) 1)
                .build();

        ProductDto dto2 = ProductDto.builder()
                .id(2L)
                .name("Mouse")
                .description("Wireless mouse")
                .price(new BigDecimal("25.50"))
                .categoryId((byte) 2)
                .build();

        when(productRepository.findAllWithCategory()).thenReturn(List.of(product1, product2));
        when(productMapper.productDto(product1)).thenReturn(dto1);
        when(productMapper.productDto(product2)).thenReturn(dto2);

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[0].description").value("Gaming laptop"))
                .andExpect(jsonPath("$[0].price").value(1200.00))
                .andExpect(jsonPath("$[0].categoryId").value(1))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Mouse"))
                .andExpect(jsonPath("$[1].description").value("Wireless mouse"))
                .andExpect(jsonPath("$[1].price").value(25.50))
                .andExpect(jsonPath("$[1].categoryId").value(2));

        verify(productRepository).findAllWithCategory();
        verify(productRepository, never()).findByCategoryId((byte) 1);
        verify(productMapper).productDto(product1);
        verify(productMapper).productDto(product2);
    }

    @Test
    @DisplayName("GET /products?categoryId=1 returns filtered products")
    void getAllProducts_withCategoryId_returnsFilteredProducts() throws Exception {
        Byte categoryId = 1;

        Product product = Product.builder()
                .id(10L)
                .name("Keyboard")
                .description("Mechanical keyboard")
                .price(new BigDecimal("99.99"))
                .build();

        ProductDto dto = ProductDto.builder()
                .id(10L)
                .name("Keyboard")
                .description("Mechanical keyboard")
                .price(new BigDecimal("99.99"))
                .categoryId(categoryId)
                .build();

        when(productRepository.findByCategoryId(categoryId)).thenReturn(List.of(product));
        when(productMapper.productDto(product)).thenReturn(dto);

        mockMvc.perform(get("/products").param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].name").value("Keyboard"))
                .andExpect(jsonPath("$[0].description").value("Mechanical keyboard"))
                .andExpect(jsonPath("$[0].price").value(99.99))
                .andExpect(jsonPath("$[0].categoryId").value(1));

        verify(productRepository).findByCategoryId(categoryId);
        verify(productRepository, never()).findAllWithCategory();
        verify(productMapper).productDto(product);
    }
}