package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.Category;
import ar.edu.itba.paw.model.Model;
import ar.edu.itba.paw.model.ModelListingInfoSummary;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Subcategory;

import java.util.List;

public interface CatalogService {
    List<Category> getAllCategories();

    List<Subcategory> getSubcategoriesByCategory(Long categoryId);

    List<Brand> getBrandsBySubcategory(Long subcategoryId);

    List<Model> getModelsBySubcategoryAndBrand(Long subcategoryId, Long brandId);

    Model getModelById(Long id);

    Page<ModelListingInfoSummary> getListingInfoSummary(Long brandId, Long modelId, Integer fromYear, Integer toYear,
            Long subcategoryId, Integer page, int pageSize);
}
