package com.shoploc.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.shoploc.user.entity.MerchantRequest;

@Repository
public interface MerchantRequestRepository extends JpaRepository<MerchantRequest, Long> {
}
