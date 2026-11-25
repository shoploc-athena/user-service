package com.shoploc.user.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.shoploc.user.entity.AppUser;
import com.shoploc.user.entity.MerchantRequest;

@Repository
public interface MerchantRequestRepository extends JpaRepository<MerchantRequest, UUID> {
    Optional<MerchantRequest> findFirstByUser_IdOrderByCreatedAtDesc(UUID userId);
    Optional<MerchantRequest> findByUser(AppUser user);
}