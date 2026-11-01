package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RequiredArgsConstructor
@Controller
@PreAuthorize("permitAll()")
public class ImageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageController.class);

    private final ImageService imageService;

    @GetMapping(value = "/image/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable("id") Long id) {
        LOGGER.debug("Accessing image {}", id);
        return imageService.getById(id)
                .map(image -> ResponseEntity.ok()
                        .contentType(image.getContentType()
                                .map(MediaType::parseMediaType)
                                .orElse(MediaType.APPLICATION_OCTET_STREAM)
                        )
                        .body(image.getData()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
