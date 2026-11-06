package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Brand;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface BrandDao {
    Optional<Brand> getById(Long id);

    Optional<Brand> getByName(String name);

    List<Brand> getAll();

    List<Brand> getBySubcategoryId(Long subcategoryId);

    Optional<Brand> rename(Long id, String name);

    boolean delete(Long id);

    Map<Long, Long> getModelCountsByBrand();

    Set<Long> getBrandIdsWithListings();
}
