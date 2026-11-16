package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.ListingService;
import ar.edu.itba.paw.service.OfferService;
import ar.edu.itba.paw.service.exception.BadParameterException;
import ar.edu.itba.paw.webapp.form.CheckoutForm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

@RunWith(MockitoJUnitRunner.class)
public class CheckoutControllerTest {

    @Mock
    private ListingService listingService;

    @Mock
    private OfferService offerService;

    @InjectMocks
    private CheckoutController controller;

    @Test(expected = BadParameterException.class)
    public void testCheckoutPostWithoutListingIdThrows() {
        // Arrange
        final User user = User.builder().id(1L).build();
        final CheckoutForm form = new CheckoutForm();
        final BindingResult bindingResult = new BeanPropertyBindingResult(form, "checkoutForm");
        bindingResult.reject("invalid");

        // Act
        controller.checkoutPost(form, bindingResult, user);
    }
}
