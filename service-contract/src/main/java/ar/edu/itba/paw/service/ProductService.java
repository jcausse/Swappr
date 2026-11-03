package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Category;
import ar.edu.itba.paw.model.Product;
import ar.edu.itba.paw.model.Subcategory;
import java.util.List;

public interface ProductService {
    Product getById(Long id);

    List<Category> getAllCategories();
    List<Subcategory> getSubcategoriesByCategory(Long categoryId);
    List<String> getBrandsBySubcategory(Long subcategoryId);
    List<String> getModelsBySubcategoryAndBrand(Long subcategoryId, String brand);

    Product findOrCreateByBrandModelYear(String brand, String model, Integer year, Long subcategoryId);
}
