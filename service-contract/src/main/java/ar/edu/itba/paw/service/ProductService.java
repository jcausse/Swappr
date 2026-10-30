package ar.edu.itba.paw.service;

import java.util.List;

import ar.edu.itba.paw.model.Category;
import ar.edu.itba.paw.model.Product;
import ar.edu.itba.paw.model.ProductListingInfoSummary;
import ar.edu.itba.paw.model.Subcategory;
import ar.edu.itba.paw.service.dto.ProductCreationDto;

public interface ProductService {
    Product getById(Long id);

    List<Product> getByCategory(Long categoryId);
    List<Product> getBySubcategory(Long subcategoryId);
    List<Product> getBySubcategoryBrandModel(Long subcategoryId, String brand, String model);

    List<Category> getAllCategories();
    List<Subcategory> getSubcategoriesByCategory(Long categoryId);
    List<String> getBrandsBySubcategory(Long subcategoryId);
    List<String> getModelsBySubcategoryAndBrand(Long subcategoryId, String brand);

    Product findOrCreateByBrandModelYear(String brand, String model, Integer year, Long subcategoryId);

    List<ProductListingInfoSummary> getListingInfoSummary(String brand, String model, Integer fromYear, Integer toYear, Long subcategoryId);

    Product create(ProductCreationDto dto);
}
