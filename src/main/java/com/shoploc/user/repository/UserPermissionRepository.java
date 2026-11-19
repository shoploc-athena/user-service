package com.shoploc.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.shoploc.user.entity.UserPermission;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {
}
