package ar.edu.itba.paw.model;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
public class Price {

    @Column(name = "price", nullable = false, precision = 100, scale = 2)
    private @NonNull BigDecimal amount;
}
