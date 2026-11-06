package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Brand;
import java.util.List;
import java.util.Optional;

public interface BrandDao {
    Optional<Brand> getById(Long id);

    List<Brand> getBySubcategoryId(Long subcategoryId);
}
