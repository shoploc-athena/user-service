package com.shoploc.user.exception;

public class EmailAlreadyExistsException extends RuntimeException {
	public EmailAlreadyExistsException() {
		super("Email already exists");
	}
}
