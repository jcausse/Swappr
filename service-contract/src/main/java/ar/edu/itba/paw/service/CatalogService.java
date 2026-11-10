package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.BrandInfo;
import ar.edu.itba.paw.model.BrandSort;
import ar.edu.itba.paw.model.Category;
import ar.edu.itba.paw.model.Model;
import ar.edu.itba.paw.model.ModelInfo;
import ar.edu.itba.paw.model.ModelListingInfoSummary;
import ar.edu.itba.paw.model.ModelSort;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Subcategory;
import ar.edu.itba.paw.service.dto.ModelOption;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CatalogService {
    Page<BrandInfo> getBrands(BrandSort sort, Integer page, int pageSize);

    List<Brand> getAllBrands();

    Brand getBrandById(Long id);

    Map<Long, List<Brand>> getBrandReplacementOptions();

    Brand createBrand(String name);

    void replaceBrand(Long id, Long replacementId);

    void renameBrand(Long id, String name);

    void deleteBrand(Long id);

    List<Category> getAllCategories();

    List<Subcategory> getSubcategoriesByCategory(Long categoryId);

    List<Subcategory> getAllSubcategories();

    Subcategory getSubcategoryById(Long id);

    List<Brand> getBrandsBySubcategory(Long subcategoryId);

    List<Model> getModelsBySubcategoryAndBrand(Long subcategoryId, Long brandId);

    Model getModelById(Long id);

    Page<ModelInfo> getModels(Long brandId, Long subcategoryId, ModelSort sort, Integer page, int pageSize);

    List<Model> getAllModels();

    Map<Long, List<ModelOption>> getModelReplacementOptions();

    Model createModel(String name, Long brandId, Long subcategoryId);

    void updateModel(Long id, String name, Long brandId, Long subcategoryId);

    void deleteModel(Long id);

    void replaceModel(Long id, Long replacementId);

    Page<ModelListingInfoSummary> getListingInfoSummary(Long brandId, Long modelId, Integer fromYear, Integer toYear,
            Long subcategoryId, Integer page, int pageSize);

    Optional<ModelListingInfoSummary> getListingInfoSummaryForModel(Long modelId);
}
