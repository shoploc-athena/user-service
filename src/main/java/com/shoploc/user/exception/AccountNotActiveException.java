package com.shoploc.user.exception;

import com.shoploc.user.entity.MerchantRequestStatus;

/** Correct credentials, but the account cannot log in (pending or disabled). */
public class AccountNotActiveException extends RuntimeException {

    private final String code;
    private final MerchantRequestStatus merchantRequestStatus;
    private final String refusalReason;

    public AccountNotActiveException(String code, MerchantRequestStatus merchantRequestStatus, String refusalReason) {
        super(code);
        this.code = code;
        this.merchantRequestStatus = merchantRequestStatus;
        this.refusalReason = refusalReason;
    }

    public String getCode() { return code; }
    public MerchantRequestStatus getMerchantRequestStatus() { return merchantRequestStatus; }
    public String getRefusalReason() { return refusalReason; }
}
