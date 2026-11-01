package ar.edu.itba.paw.webapp.auth;

import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.webapp.exception.UserNotAuthenticatedException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthHelper authHelper;

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && parameter.getParameterType().equals(User.class);
    }

    @Override
    public Object resolveArgument(final MethodParameter parameter,
                                  final ModelAndViewContainer mavContainer,
                                  @NonNull final NativeWebRequest webRequest,
                                  final WebDataBinderFactory binderFactory) {
        final var annotation = parameter.getParameterAnnotation(CurrentUser.class);
        final var currentUser = authHelper.getCurrentUser();

        if (currentUser.isPresent()) {
            return currentUser.get();
        }

        if (annotation != null && !annotation.required()) {
            return null;
        }

        throw new UserNotAuthenticatedException();
    }
}
