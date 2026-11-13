package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.context.request.WebRequest;

import java.time.Duration;

@RequiredArgsConstructor
@Controller
@PreAuthorize("permitAll()")
public class ImageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageController.class);
    private static final CacheControl IMAGE_CACHE_CONTROL =
            CacheControl.maxAge(Duration.ofDays(1)).cachePublic();

    private final ImageService imageService;

    @GetMapping(value = "/image/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable("id") Long id, WebRequest request) {
        LOGGER.debug("Accessing image {}", id);
        final var maybeImage = imageService.getById(id);
        if (maybeImage.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        final var image = maybeImage.get();
        final String etag = "image-" + image.getId();
        if (request.checkNotModified(etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(etag)
                    .cacheControl(IMAGE_CACHE_CONTROL)
                    .build();
        }

        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(IMAGE_CACHE_CONTROL)
                .contentType(image.getContentType()
                        .map(MediaType::parseMediaType)
                        .orElse(MediaType.APPLICATION_OCTET_STREAM)
                )
                .body(image.getData());
    }
}
