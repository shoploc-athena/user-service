package com.shoploc.user.dto;

/** expiresIn is expressed in seconds. */
public record LoginResponse(String accessToken, String tokenType, long expiresIn) {
}
