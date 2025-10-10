package com.friday.commerce.user.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_addresses")
@Entity
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long addressId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "zip_code", length = 20, nullable = false)
    private String zipCode;

    @Column(name = "address_line1", length = 200, nullable = false)
    private String addressLine1;

    @Column(name = "address_line2", length = 200)
    private String addressLine2;

    @Column(name = "city", length = 100, nullable = false)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = Boolean.FALSE;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserAddress that = (UserAddress) o;
        return addressId != null && addressId.equals(that.addressId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(addressId);
    }

    @Builder(access = AccessLevel.PRIVATE)
    private UserAddress(
            User user,
            String zipCode,
            String addressLine1,
            String addressLine2,
            String city,
            String state,
            Boolean isDefault
    ) {
        this.user = user;
        this.zipCode = zipCode;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.state = state;
        this.isDefault = (isDefault != null) ? isDefault : Boolean.FALSE;
    }

    public static UserAddress create(
            String zipCode,
            String addressLine1,
            String addressLine2,
            String city,
            String state
    ) {
        return UserAddress.builder()
                .zipCode(zipCode)
                .addressLine1(addressLine1)
                .addressLine2(addressLine2)
                .city(city)
                .state(state)
                .build();
    }

    void setUser(User user) {
        this.user = user;
    }

    void setDefault(boolean value) {
        this.isDefault = value;
    }
}
