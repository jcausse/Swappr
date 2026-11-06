package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.BrandInfo;
import java.util.List;
import java.util.Optional;

public interface BrandDao {
    Optional<Brand> getById(Long id);

    Optional<Brand> getByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    List<BrandInfo> getAllWithStats();

    List<Brand> getBySubcategoryId(Long subcategoryId);

    Optional<Brand> rename(Long id, String name);

    boolean delete(Long id);

    boolean hasListings(Long brandId);

    Brand create(String name);
}
