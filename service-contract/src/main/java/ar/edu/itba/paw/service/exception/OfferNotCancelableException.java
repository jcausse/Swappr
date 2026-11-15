package ar.edu.itba.paw.service.exception;

public class OfferNotCancelableException extends BadParameterException {

    public OfferNotCancelableException(Long offerId) {
        super("Offer with ID " + offerId + " cannot be canceled in its current state");
    }
}
