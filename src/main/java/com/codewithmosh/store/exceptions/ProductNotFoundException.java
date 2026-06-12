package com.codewithmosh.store.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProductNotFoundException extends RuntimeException{

    @Serial
    private static final long serialVersionUID = 1L;
    @Getter
    private final String message;


    public ProductNotFoundException(String message) {
        this.message = message;

    }
}
