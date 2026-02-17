package com.biogenholdings.InventoryMgtSystem.exceptions;

public class NameValueRequiredException extends  RuntimeException{

    public NameValueRequiredException(String message){
        super(message);
    }
}
