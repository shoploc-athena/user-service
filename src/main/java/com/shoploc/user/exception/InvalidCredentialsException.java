package com.shoploc.user.exception;

/** Same exception for "unknown email" and "wrong password" (we never say which one). */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}