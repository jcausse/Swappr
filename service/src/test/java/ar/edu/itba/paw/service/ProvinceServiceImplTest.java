package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Province;
import ar.edu.itba.paw.persistence.ProvinceDao;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ProvinceServiceImplTest {

    private static final long PROVINCE_ID = 1L;
    private static final long NON_EXISTING_ID = 9000L;
    private static final String PROVINCE_NAME = "buenos_aires";

    @InjectMocks
    private ProvinceServiceImpl provinceService;

    @Mock
    private ProvinceDao provinceDao;

    private Province buildProvince() {
        return Province.builder().id(PROVINCE_ID).name(PROVINCE_NAME).build();
    }

    /* ---------------------------------------------------------------------------------------------- */
    /* getById                                                                                         */
    /* ---------------------------------------------------------------------------------------------- */

    @Test
    public void testGetByIdExists() {
        // Arrange
        when(provinceDao.getById(eq(PROVINCE_ID))).thenReturn(Optional.of(buildProvince()));

        // Act
        final Optional<Province> result = provinceService.getById(PROVINCE_ID);

        // Assert
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(PROVINCE_NAME, result.get().getName());
    }

    @Test
    public void testGetByIdDoesNotExist() {
        // Arrange
        when(provinceDao.getById(eq(NON_EXISTING_ID))).thenReturn(Optional.empty());

        // Act
        final Optional<Province> result = provinceService.getById(NON_EXISTING_ID);

        // Assert
        Assert.assertTrue(result.isEmpty());
    }
}
