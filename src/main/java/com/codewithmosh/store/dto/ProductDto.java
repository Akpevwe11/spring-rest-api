package com.codewithmosh.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ProductDto {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Byte categoryId;


}
