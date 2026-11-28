package com.shoploc.user.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import com.shoploc.user.entity.MerchantRequest;
import com.shoploc.user.entity.MerchantRequestStatus;

public record MerchantRequestResponse(
        UUID id,
        UUID userId,
        String storeName,
        String storeAddress,
        MerchantRequestStatus status,
        String refusalReason,
        UUID reviewedBy,
        OffsetDateTime reviewedAt,
        OffsetDateTime createdAt) {

    public static MerchantRequestResponse from(MerchantRequest request) {
        UUID reviewerId = request.getReviewedBy() == null ? null : request.getReviewedBy().getId();
        return new MerchantRequestResponse(
                request.getId(),
                request.getUser().getId(),
                request.getStoreName(),
                request.getStoreAddress(),
                request.getStatus(),
                request.getRefusalReason(),
                reviewerId,
                request.getReviewedAt(),
                request.getCreatedAt());
    }
}