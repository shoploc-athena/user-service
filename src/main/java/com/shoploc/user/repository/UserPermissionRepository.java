package com.shoploc.user.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.shoploc.user.entity.UserPermission;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, UUID> {
    List<UserPermission> findByUser_Id(UUID userId);
}