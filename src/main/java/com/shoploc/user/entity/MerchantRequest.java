package com.shoploc.user.entity;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "merchant_request", indexes = {@Index(name = "idx_merchant_request_user_id", columnList = "user_id"), @Index(name = "idx_merchant_request_status", columnList = "status")})
public class MerchantRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;
    @Column(nullable = false, length = 150)
    private String storeName;
    @Column(nullable = false, length = 255)
    private String storeAddress;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MerchantRequestStatus status;
    @Column(length = 255)
    private String refusalReason;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private AppUser reviewedBy;
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    @Column
    private OffsetDateTime reviewedAt;
    @Version
    private Long version;

    public MerchantRequest() {}
    public MerchantRequest(AppUser user, String storeName, String storeAddress) {
        this.user = user; this.storeName = storeName; this.storeAddress = storeAddress; this.status = MerchantRequestStatus.PENDING;
    }
    @PrePersist
    protected void onCreate() { createdAt = OffsetDateTime.now(); }

    @PreUpdate
    protected void onUpdate() {
        if (reviewedBy != null && reviewedAt == null) {
            reviewedAt = OffsetDateTime.now();
        }
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public String getStoreAddress() { return storeAddress; }
    public void setStoreAddress(String storeAddress) { this.storeAddress = storeAddress; }
    public MerchantRequestStatus getStatus() { return status; }
    public void setStatus(MerchantRequestStatus status) { this.status = status; }
    public String getRefusalReason() { return refusalReason; }
    public void setRefusalReason(String refusalReason) { this.refusalReason = refusalReason; }
    public AppUser getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(AppUser reviewedBy) { this.reviewedBy = reviewedBy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(OffsetDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
