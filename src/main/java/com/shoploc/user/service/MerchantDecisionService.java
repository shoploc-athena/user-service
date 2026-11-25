package com.shoploc.user.service;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.shoploc.user.dto.MerchantRequestResponse;
import com.shoploc.user.entity.MerchantRequest;
import com.shoploc.user.entity.MerchantRequestStatus;
import com.shoploc.user.entity.UserStatus;
import com.shoploc.user.exception.MerchantRequestAlreadyDecidedException;
import com.shoploc.user.exception.MerchantRequestNotFoundException;
import com.shoploc.user.repository.AppUserRepository;
import com.shoploc.user.repository.MerchantRequestRepository;

/** Lets the association accept or refuse a merchant request. */
@Service
public class MerchantDecisionService {

    private final MerchantRequestRepository merchantRequestRepository;
    private final AppUserRepository appUserRepository;

    public MerchantDecisionService(MerchantRequestRepository merchantRequestRepository,
                                   AppUserRepository appUserRepository) {
        this.merchantRequestRepository = merchantRequestRepository;
        this.appUserRepository = appUserRepository;
    }

    /** Accepts the request and activates the merchant account, in one transaction. */
    @Transactional
    public MerchantRequestResponse accept(UUID requestId, UUID reviewerId) {
        MerchantRequest request = review(requestId, reviewerId, MerchantRequestStatus.ACCEPTED);
        request.getUser().setStatus(UserStatus.ACTIVE);
        return save(request);
    }

    /** Refuses the request: the account stays PENDING and cannot log in. */
    @Transactional
    public MerchantRequestResponse reject(UUID requestId, UUID reviewerId, String refusalReason) {
        MerchantRequest request = review(requestId, reviewerId, MerchantRequestStatus.REFUSED);
        request.setRefusalReason(refusalReason);
        return save(request);
    }

    /** Checks the request is PENDING, then fills the fields shared by both decisions. */
    private MerchantRequest review(UUID requestId, UUID reviewerId, MerchantRequestStatus decision) {
        MerchantRequest request = merchantRequestRepository.findById(requestId)
                .orElseThrow(() -> new MerchantRequestNotFoundException(requestId));
        if (request.getStatus() != MerchantRequestStatus.PENDING) {
            throw new MerchantRequestAlreadyDecidedException(requestId, request.getStatus());
        }
        request.setStatus(decision);
        request.setReviewedBy(appUserRepository.getReferenceById(reviewerId));
        request.setReviewedAt(OffsetDateTime.now());
        return request;
    }

    private MerchantRequestResponse save(MerchantRequest request) {
        return MerchantRequestResponse.from(merchantRequestRepository.saveAndFlush(request));
    }
}
