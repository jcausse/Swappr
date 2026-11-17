package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.File;
import ar.edu.itba.paw.model.FileMetadata;
import ar.edu.itba.paw.persistence.FileDao;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileServiceImplTest {

    private static final long FILE_ID = 1L;
    private static final String FILENAME = "proof.pdf";
    private static final String ALT = "Proof of payment";
    private static final String CONTENT_TYPE = "application/pdf";
    private static final byte[] DATA = new byte[]{1, 2, 3};

    @InjectMocks
    private FileServiceImpl fileService;

    @Mock
    private FileDao fileDao;

    private File buildFakeFile() {
        return File.builder()
            .id(FILE_ID)
            .filename(FILENAME)
            .alt(ALT)
            .contentType(CONTENT_TYPE)
            .data(DATA)
            .build();
    }

    @Test
    public void testGetByIdExists() {
        // Arrange
        final File file = buildFakeFile();
        when(fileDao.getById(eq(FILE_ID))).thenReturn(Optional.of(file));

        // Act
        final Optional<File> result = fileService.getById(FILE_ID);

        // Assert
        Assert.assertTrue(result.isPresent());
        Assert.assertSame(file, result.get());
    }

    @Test
    public void testGetMetadataByIdsDelegatesToDao() {
        // Arrange
        final Map<Long, FileMetadata> metadata = Map.of(FILE_ID,
            new FileMetadata(FILE_ID, FILENAME, ALT, CONTENT_TYPE, (long) DATA.length));
        when(fileDao.getMetadataByIds(eq(List.of(FILE_ID)))).thenReturn(metadata);

        // Act
        final Map<Long, FileMetadata> result = fileService.getMetadataByIds(List.of(FILE_ID));

        // Assert
        Assert.assertSame(metadata, result);
    }

    @Test
    public void testCreateDelegatesToDao() {
        // Arrange
        final File file = buildFakeFile();
        when(fileDao.create(eq(FILENAME), eq(ALT), eq(CONTENT_TYPE), eq(DATA))).thenReturn(file);

        // Act
        final File result = fileService.create(FILENAME, ALT, CONTENT_TYPE, DATA);

        // Assert
        Assert.assertSame(file, result);
    }
}
