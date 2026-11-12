package ar.edu.itba.paw.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationMode {
    DISABLED(false, false),
    SITE_ONLY(true, false),
    EMAIL_ONLY(false, true),
    SITE_AND_EMAIL(true, true);

    private final boolean siteEnabled;
    private final boolean emailEnabled;
}
