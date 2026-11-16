package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.CatalogService;
import ar.edu.itba.paw.service.ListingCancellationService;
import ar.edu.itba.paw.service.ListingService;
import ar.edu.itba.paw.service.OfferService;
import ar.edu.itba.paw.service.ProvinceService;
import ar.edu.itba.paw.service.exception.BadParameterException;
import ar.edu.itba.paw.webapp.form.ListingDetailsForm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

@RunWith(MockitoJUnitRunner.class)
public class ListingControllerTest {

    @Mock
    private ListingService listingService;

    @Mock
    private ListingCancellationService listingCancellationService;

    @Mock
    private OfferService offerService;

    @Mock
    private CatalogService catalogService;

    @Mock
    private ProvinceService provinceService;

    @InjectMocks
    private ListingController controller;

    @Test(expected = BadParameterException.class)
    public void testDetailsPostWithoutModelIdThrows() {
        // Arrange
        final User user = User.builder().id(1L).build();
        final ListingDetailsForm form = new ListingDetailsForm();
        final BindingResult bindingResult = new BeanPropertyBindingResult(form, "detailsForm");
        bindingResult.reject("invalid");

        // Act
        controller.detailsPost(form, bindingResult, user);
    }

    @Test(expected = BadParameterException.class)
    public void testEditListingWithoutModelIdThrows() {
        // Arrange
        final User user = User.builder().id(1L).build();
        final ListingDetailsForm form = new ListingDetailsForm();
        final BindingResult bindingResult = new BeanPropertyBindingResult(form, "detailsForm");
        bindingResult.reject("invalid");

        // Act
        controller.editListing(1L, form, bindingResult, user);
    }
}
