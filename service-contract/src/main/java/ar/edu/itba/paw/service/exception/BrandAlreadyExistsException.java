package ar.edu.itba.paw.service.exception;

public class BrandAlreadyExistsException extends BadParameterException {

    public BrandAlreadyExistsException(String brandName) {
        super("Brand '" + brandName + "' already exists");
    }
}
