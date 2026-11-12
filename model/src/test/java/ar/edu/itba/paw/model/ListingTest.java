package ar.edu.itba.paw.model;

import org.junit.Assert;
import org.junit.Test;

import java.time.Year;

public class ListingTest {

    @Test
    public void testIsValidYearAcceptsMinimumYear() {
        Assert.assertTrue(Listing.isValidYear(Listing.MIN_YEAR));
    }

    @Test
    public void testIsValidYearRejectsYearBeforeMinimum() {
        Assert.assertFalse(Listing.isValidYear(Listing.MIN_YEAR - 1));
    }

    @Test
    public void testIsValidYearAcceptsCurrentYear() {
        Assert.assertTrue(Listing.isValidYear(Year.now().getValue()));
    }

    @Test
    public void testIsValidYearRejectsFutureYear() {
        Assert.assertFalse(Listing.isValidYear(Year.now().getValue() + 1));
    }
}
