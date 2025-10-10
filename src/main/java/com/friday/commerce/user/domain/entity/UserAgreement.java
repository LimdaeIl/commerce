package com.friday.commerce.user.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class UserAgreement {

    @Column(name = "agree_terms", nullable = false)
    private boolean termsOfService = Boolean.FALSE;

    @Column(name = "agree_privacy", nullable = false)
    private boolean privacy = Boolean.FALSE;

    @Column(name = "agree_marketing", nullable = false)
    private boolean marketing = Boolean.FALSE;

    @Column(name = "agree_at", nullable = false)
    private LocalDateTime agreedAt;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserAgreement serviceAgreement = (UserAgreement) o;
        return termsOfService == serviceAgreement.termsOfService
                && privacy == serviceAgreement.privacy
                && marketing == serviceAgreement.marketing
                && Objects.equals(agreedAt, serviceAgreement.agreedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(termsOfService, privacy, marketing, agreedAt);
    }

    @Builder(access = AccessLevel.PRIVATE)
    private UserAgreement(
            Boolean termsOfService,
            Boolean privacy,
            Boolean marketing
    ) {
        this.termsOfService = termsOfService;
        this.privacy = privacy;
        this.marketing = marketing;
        this.agreedAt = LocalDateTime.now();
    }

    public static UserAgreement create(
            Boolean termsOfService,
            Boolean privacy,
            Boolean marketing
    ) {
        return UserAgreement.builder()
                .termsOfService(termsOfService)
                .privacy(privacy)
                .marketing(marketing)
                .build();
    }
}
