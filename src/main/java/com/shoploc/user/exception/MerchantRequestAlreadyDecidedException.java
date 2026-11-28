package com.shoploc.user.exception;

import java.util.UUID;
import com.shoploc.user.entity.MerchantRequestStatus;

/** The merchant request is not PENDING anymore (HTTP 409). */
public class MerchantRequestAlreadyDecidedException extends RuntimeException {

    public MerchantRequestAlreadyDecidedException(UUID id, MerchantRequestStatus status) {
        super("Merchant request " + id + " is already " + status);
    }
}
