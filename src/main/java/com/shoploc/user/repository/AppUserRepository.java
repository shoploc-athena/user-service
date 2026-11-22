package com.shoploc.user.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.shoploc.user.entity.AppUser;
import com.shoploc.user.entity.UserRole;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
	Optional<AppUser> findByEmail(String email);
	boolean existsByRole(UserRole role);
}
