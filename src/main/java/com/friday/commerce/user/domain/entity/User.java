package com.friday.commerce.user.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
@Entity
public class User {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 1024)
    private String password;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Embedded
    private UserAgreement userAgreement;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "order_index")
    private List<UserAddress> userAddresses = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, length = 30)
    private UserRole userRole = UserRole.USER;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    @Column(name = "is_locked", nullable = false)
    private Boolean isLocked = Boolean.FALSE;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        User other = (User) o;
        return userId != null && userId.equals(other.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(userId);
    }

    @Builder(access = AccessLevel.PRIVATE)
    private User(
            Long userId,
            String email,
            String password,
            String username,
            UserAgreement userAgreement,
            UserAddress userAddresses
    ) {
        LocalDateTime now = LocalDateTime.now();
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.username = username;
        this.userAgreement = userAgreement;
        this.createdAt = now;
        this.updatedAt = null;
        this.updatedBy = null;
        this.deletedAt = null;
        this.deletedBy = null;

        if (userAddresses != null) {
            addAddress(userAddresses);
        }
    }

    public static User create(
            Long userId,
            String email,
            String password,
            String username,
            UserAgreement userAgreement,
            UserAddress userAddresses
    ) {
        return User.builder()
                .userId(userId)
                .email(email)
                .password(password)
                .username(username)
                .userAgreement(userAgreement)
                .userAddresses(userAddresses)
                .build();
    }

    public void addAddress(UserAddress address) {
        if (address == null) {
            return;
        }
        this.userAddresses.add(address);
        address.setUser(this);
    }

    public void removeAddress(UserAddress address) {
        if (address == null) {
            return;
        }
        this.userAddresses.remove(address);
        address.setUser(null);
    }

    /**
     * 기본 배송지 단일성 보장 도메인 규칙. - 모두 false로 내린 뒤, 지정 주소만 true
     */
    public void setDefaultAddress(Long addressId) {
        if (addressId == null) {
            return;
        }
        boolean found = false;
        for (UserAddress addr : userAddresses) {
            if (addressId.equals(addr.getAddressId())) {
                addr.setDefault(true);
                found = true;
            } else {
                if (Boolean.TRUE.equals(addr.getIsDefault())) {
                    addr.setDefault(false);
                }
            }
        }
        if (!found) {
            throw new IllegalArgumentException("Address not owned by this user: " + addressId);
        }
    }

    public com.friday.commerce.core.security.model.UserRole toCoreRole() {
        return switch (this.userRole) {
            case USER -> com.friday.commerce.core.security.model.UserRole.USER;
            case SELLER -> com.friday.commerce.core.security.model.UserRole.SELLER;
            case ADMIN -> com.friday.commerce.core.security.model.UserRole.ADMIN;
        };
    }
}
