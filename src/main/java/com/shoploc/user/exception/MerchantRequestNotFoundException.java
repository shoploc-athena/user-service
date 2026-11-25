package com.shoploc.user.exception;

import java.util.UUID;

/** The merchant request does not exist (HTTP 404). */
public class MerchantRequestNotFoundException extends RuntimeException {

    public MerchantRequestNotFoundException(UUID id) {
        super("Merchant request not found: " + id);
    }
}
