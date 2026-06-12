package com.codewithmosh.store.mappers;

import com.codewithmosh.store.dto.ProductDto;
import com.codewithmosh.store.entities.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "category.id", target = "categoryId")
    ProductDto productDto(Product product);

    List<ProductDto> productsDto(List<Product> products);

    Product toEntity(ProductDto productDto);

}