package com.codewithmosh.store.mappers;
import com.codewithmosh.store.dto.CartDto;
import com.codewithmosh.store.entities.Cart;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CartMapper {

    CartDto toDto(Cart cart);


}
