package ar.edu.itba.paw.service.exception;

public class CatalogEntityInUseException extends BadParameterException {

    public CatalogEntityInUseException(String message) {
        super(message);
    }
}
