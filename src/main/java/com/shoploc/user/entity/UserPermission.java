package com.shoploc.user.entity;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "user_permission", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "permission"})})
public class UserPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_by")
    private AppUser grantedBy;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PermissionType permission;
    @Version
    private Long version;

    public UserPermission() {}
    public UserPermission(AppUser user, AppUser grantedBy, PermissionType permission) {
        this.user = user; this.grantedBy = grantedBy; this.permission = permission;
    }
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public AppUser getGrantedBy() { return grantedBy; }
    public void setGrantedBy(AppUser grantedBy) { this.grantedBy = grantedBy; }
    public PermissionType getPermission() { return permission; }
    public void setPermission(PermissionType permission) { this.permission = permission; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
