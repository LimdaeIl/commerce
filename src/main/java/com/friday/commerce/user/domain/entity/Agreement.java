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
public class Agreement {

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
        Agreement agreement = (Agreement) o;
        return termsOfService == agreement.termsOfService
                && privacy == agreement.privacy
                && marketing == agreement.marketing
                && Objects.equals(agreedAt, agreement.agreedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(termsOfService, privacy, marketing, agreedAt);
    }

    @Builder(access = AccessLevel.PRIVATE)
    private Agreement(
            Boolean termsOfService,
            Boolean privacy,
            Boolean marketing
    ) {
        this.termsOfService = termsOfService;
        this.privacy = privacy;
        this.marketing = marketing;
        this.agreedAt = LocalDateTime.now();
    }

    public static Agreement create(
            Boolean termsOfService,
            Boolean privacy,
            Boolean marketing
    ) {
        return Agreement.builder()
                .termsOfService(termsOfService)
                .privacy(privacy)
                .marketing(marketing)
                .build();
    }
}
