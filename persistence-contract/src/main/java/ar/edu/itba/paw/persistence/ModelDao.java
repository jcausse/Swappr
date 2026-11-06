package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.Model;
import ar.edu.itba.paw.model.ModelInfo;
import ar.edu.itba.paw.model.ModelListingInfo;
import ar.edu.itba.paw.model.ModelSort;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Subcategory;
import java.util.List;
import java.util.Optional;

public interface ModelDao {
    Optional<Model> getById(Long id);

    List<Model> getBySubcategoryIdAndBrandId(Long subcategoryId, Long brandId);

    Page<ModelListingInfo> getListingInfoSummary(Long brandId, Long modelId, Integer fromYear, Integer toYear,
            Long subcategoryId, int page, int pageSize);

    Page<ModelInfo> getWithInfo(Long brandId, Long subcategoryId, ModelSort sort, int page, int pageSize);

    List<Model> getAllWithBrand();

    boolean existsByNameAndBrandId(String name, Long brandId);

    boolean existsByNameAndBrandIdAndIdNot(String name, Long brandId, Long id);

    Model create(String name, Brand brand, Subcategory subcategory);

    Optional<Model> update(Long id, String name, Brand brand, Subcategory subcategory);

    boolean deleteModel(Long id);

    boolean hasListings(Long modelId);

    int replaceBrand(Long fromBrandId, Long toBrandId);
}
