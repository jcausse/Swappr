package ar.edu.itba.paw.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@EqualsAndHashCode
@ToString
public class Price {

    @Column(name = "price", nullable = false, precision = 100, scale = 2)
    private @NonNull BigDecimal amount;

    public Price(final BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Price amount cannot be null");
        }
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Price amount cannot be negative");
        }
        this.amount = amount;
    }
}
