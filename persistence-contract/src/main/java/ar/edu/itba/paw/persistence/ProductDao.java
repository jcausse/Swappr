package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Product;
import ar.edu.itba.paw.model.ProductListingInfo;
import ar.edu.itba.paw.model.Subcategory;
import java.util.List;
import java.util.Optional;

public interface ProductDao {
    Optional<Product> getById(Long id);

    Optional<Product> getByBrandModelYearSubcategory(String brand, String model, Integer year, Long subcategoryId);

    List<String> getBrandsBySubcategory(Long subcategoryId);
    List<String> getModelsBySubcategoryAndBrand(Long subcategoryId, String brand);

    Product create(String brand, String model, Integer year, Subcategory subcategory);

    Page<ProductListingInfo> getListingInfoSummary(String brand, String model, Integer fromYear, Integer toYear, Long subcategoryId, int page, int pageSize);
}
