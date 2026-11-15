package ar.edu.itba.paw.service.exception;

public class ListingNotCancelableException extends BadParameterException {

    public ListingNotCancelableException(Long listingId) {
        super("Listing with ID " + listingId + " cannot be canceled in its current state");
    }
}
