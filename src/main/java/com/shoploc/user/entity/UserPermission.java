package com.shoploc.user.entity;
import jakarta.persistence.*;

@Entity
@Table(name = "user_permission", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "permission"})})
public class UserPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_by", nullable = false)
    private AppUser grantedBy;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PermissionType permission;

    public UserPermission() {}
    public UserPermission(AppUser user, AppUser grantedBy, PermissionType permission) {
        this.user = user; this.grantedBy = grantedBy; this.permission = permission;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public AppUser getGrantedBy() { return grantedBy; }
    public void setGrantedBy(AppUser grantedBy) { this.grantedBy = grantedBy; }
    public PermissionType getPermission() { return permission; }
    public void setPermission(PermissionType permission) { this.permission = permission; }
}
