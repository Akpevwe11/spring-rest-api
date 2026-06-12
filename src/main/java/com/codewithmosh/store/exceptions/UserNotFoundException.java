package com.codewithmosh.store.exceptions;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import lombok.Getter;

import java.io.Serial;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;
    @Getter
    private final String message;


    public UserNotFoundException(String message) {
        this.message = message;
    }
}
