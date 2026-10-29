package ar.edu.itba.paw.webapp.controller.advice;

import ar.edu.itba.paw.service.exception.BadParameterException;
import ar.edu.itba.paw.service.exception.NotFoundException;
import ar.edu.itba.paw.webapp.exception.UserNotAuthenticatedException;
import ar.edu.itba.paw.webapp.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /* Routes that do not have a mapping fall here */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNoHandlerFound(NoHandlerFoundException ex) {
        LOGGER.warn("No handler found: {}", ex.getMessage());
        return new ModelAndView("error/notFound");
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleGenericNotFound(NotFoundException ex) {
        LOGGER.warn("Resource not found: {}", ex.getMessage());
        return new ModelAndView("error/notFound");
    }

    @ExceptionHandler(BadParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleBadParameter(BadParameterException ex) {
        LOGGER.warn("Bad parameter: {}", ex.getMessage());
        return new ModelAndView("error/badRequest");
    }

    @ExceptionHandler({BindException.class, MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleBadRequestBinding(Exception ex) {
        LOGGER.warn("Bad request binding: {}", ex.getMessage());
        return new ModelAndView("error/badRequest");
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleUserNotFound(UserNotFoundException ex) {
        LOGGER.warn("User not found: {}", ex.getMessage());
        return new ModelAndView("error/notFound")
            .addObject("messageCode", "userNotFound");
    }

    @ExceptionHandler(UserNotAuthenticatedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ModelAndView handleUserNotAuthenticated(UserNotAuthenticatedException ex) {
        LOGGER.warn("Unauthenticated access attempt");
        return new ModelAndView("error/unauthorized");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleForbidden(AccessDeniedException ex) {
        LOGGER.warn("Forbidden access: {}", ex.getMessage());
        return new ModelAndView("error/forbidden");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ModelAndView handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        LOGGER.warn("Upload size exceeded: {}", ex.getMessage());
        return new ModelAndView("error/badRequest")
            .addObject("messageCode", "fileSizeExceeded");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleException(Exception ex) {
        LOGGER.error("Unexpected error", ex);
        return new ModelAndView("error/internalServerError");
    }
}
