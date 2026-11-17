package ar.edu.itba.paw.model;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class PriceTest {

    @Test
    public void testZeroPriceIsAllowed() {
        final Price price = new Price(BigDecimal.ZERO);

        Assert.assertEquals(BigDecimal.ZERO, price.getAmount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativePriceThrows() {
        new Price(new BigDecimal("-0.01"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullPriceThrows() {
        new Price(null);
    }
}
