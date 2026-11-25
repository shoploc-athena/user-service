package com.shoploc.user.dto;

import java.util.UUID;
import com.shoploc.user.entity.MerchantRequestStatus;

public class MerchantSignUpResponse {
	private UUID requestId;
	private MerchantRequestStatus status;

	public MerchantSignUpResponse(UUID requestId, MerchantRequestStatus status) {
		this.requestId = requestId;
		this.status = status;
	}

	public UUID getRequestId() {
		return requestId;
	}

	public MerchantRequestStatus getStatus() {
		return status;
	}
}
