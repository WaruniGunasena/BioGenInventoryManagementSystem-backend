package com.biogenholdings.InventoryMgtSystem.exceptions;

public class UserExistException extends RuntimeException {
    public UserExistException(String message) {
        super(message);
    }
}
