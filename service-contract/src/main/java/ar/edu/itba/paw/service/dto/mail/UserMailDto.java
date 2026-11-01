package ar.edu.itba.paw.service.dto.mail;

import ar.edu.itba.paw.model.Language;
import ar.edu.itba.paw.model.User;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class UserMailDto {

    Long id;
    String displayName;
    String email;
    Language preferredLanguage;

    public static UserMailDto from(final @NonNull User user) {
        return UserMailDto.builder()
                .id(user.getId())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .preferredLanguage(user.getPreferredLanguage())
                .build();
    }
}
