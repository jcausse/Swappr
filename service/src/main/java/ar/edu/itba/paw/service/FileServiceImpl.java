package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.File;
import ar.edu.itba.paw.model.FileMetadata;
import ar.edu.itba.paw.persistence.FileDao;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileServiceImpl implements FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileServiceImpl.class);

    private final FileDao fileDao;

    @Override
    public Optional<File> getById(Long id) {
        return fileDao.getById(id);
    }

    @Override
    public Map<Long, FileMetadata> getMetadataByIds(Collection<Long> ids) {
        return fileDao.getMetadataByIds(ids);
    }

    @Override
    @Transactional
    public File create(String filename, String alt, String contentType, byte[] data) {
        LOGGER.debug("Creating file: filename='{}', contentType='{}', size={} bytes",
                filename, contentType, data != null ? data.length : 0);
        final File file = fileDao.create(filename, alt, contentType, data);
        LOGGER.info("File created with id={}", file.getId());
        return file;
    }
}
