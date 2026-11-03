package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.File;
import java.util.Optional;

public interface FileDao {
    Optional<File> getById(Long id);
    
    File create(String filename, String alt, String contentType, byte[] data);
}