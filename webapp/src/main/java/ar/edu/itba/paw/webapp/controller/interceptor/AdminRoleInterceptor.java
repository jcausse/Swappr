package ar.edu.itba.paw.webapp.controller.interceptor;

import ar.edu.itba.paw.service.PermissionService;
import ar.edu.itba.paw.webapp.auth.AuthHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Exposes whether the current user has ADMIN role to rendered views only (e.g. navbar),
// avoiding extra database queries on redirects and image/file responses.
@RequiredArgsConstructor
public class AdminRoleInterceptor implements HandlerInterceptor {

    private static final String IS_ADMIN_ATTRIBUTE = "isAdmin";

    private final AuthHelper authHelper;
    private final PermissionService permissionService;

    @Override
    public void postHandle(
            @NonNull final HttpServletRequest request,
            @NonNull final HttpServletResponse response,
            @NonNull final Object handler,
            final ModelAndView modelAndView
    ) {
        if (!InterceptorUtils.rendersView(modelAndView) || modelAndView.getModel().containsKey(IS_ADMIN_ATTRIBUTE)) {
            return;
        }
        final boolean isAdmin = authHelper.getCurrentUser()
                .map(permissionService::isAdmin)
                .orElse(false);
        modelAndView.addObject(IS_ADMIN_ATTRIBUTE, isAdmin);
    }
}
