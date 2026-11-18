package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.File;
import ar.edu.itba.paw.model.FileMetadata;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface FileService {

    Optional<File> getById(Long id);

    Map<Long, FileMetadata> getMetadataByIds(Collection<Long> ids);

    File create(String filename, String alt, String contentType, byte[] data);
}
