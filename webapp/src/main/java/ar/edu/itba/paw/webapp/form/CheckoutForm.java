package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.model.OfferType;
import ar.edu.itba.paw.webapp.form.validation.ValidCheckout;
import java.math.BigDecimal;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@ValidCheckout
public class CheckoutForm {

    @NotNull
    private Long listingId;

    @NotNull
    private OfferType offerType = OfferType.FULL;

    @DecimalMin(value = "0.01", message = "{Min.checkoutForm.customAmount}")
    @Digits(integer = 9, fraction = 2)
    private BigDecimal customAmount;

    private Long offeredListingId;

    private Integer page;

    public Integer getPage() {
        return page == null || page < 1 ? 1 : page;
    }

    @DecimalMin(value = "0.0", message = "{Min.checkoutForm.tradeAmount}")
    @Digits(integer = 9, fraction = 2)
    private BigDecimal tradeAmount;

    @Size(max = 1000)
    private String message;
}
