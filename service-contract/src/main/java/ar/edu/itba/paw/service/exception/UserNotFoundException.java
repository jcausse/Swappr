package ar.edu.itba.paw.service.exception;

public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotFoundException(Throwable cause) {
        super(cause);
    }

    public static UserNotFoundException byId(Long id) {
        return new UserNotFoundException("User with ID " + id + " not found");
    }

}
