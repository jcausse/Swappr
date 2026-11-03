package ar.edu.itba.paw.service;

import java.util.List;

import ar.edu.itba.paw.model.Category;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Product;
import ar.edu.itba.paw.model.ProductListingInfoSummary;
import ar.edu.itba.paw.model.Subcategory;

public interface ProductService {
    Product getById(Long id);

    List<Category> getAllCategories();
    List<Subcategory> getSubcategoriesByCategory(Long categoryId);
    List<String> getBrandsBySubcategory(Long subcategoryId);
    List<String> getModelsBySubcategoryAndBrand(Long subcategoryId, String brand);

    Product findOrCreateByBrandModelYear(String brand, String model, Integer year, Long subcategoryId);

    Page<ProductListingInfoSummary> getListingInfoSummary(String brand, String model, Integer fromYear, Integer toYear, Long subcategoryId, Integer page, int pageSize);
}
