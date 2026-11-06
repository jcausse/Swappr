package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.BrandInfo;
import ar.edu.itba.paw.model.BrandSort;
import ar.edu.itba.paw.model.Category;
import ar.edu.itba.paw.model.Model;
import ar.edu.itba.paw.model.ModelInfo;
import ar.edu.itba.paw.model.ModelListingInfo;
import ar.edu.itba.paw.model.ModelListingInfoSummary;
import ar.edu.itba.paw.model.ModelSort;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Subcategory;
import ar.edu.itba.paw.persistence.BrandDao;
import ar.edu.itba.paw.persistence.CategoryDao;
import ar.edu.itba.paw.persistence.ListingDao;
import ar.edu.itba.paw.persistence.ModelDao;
import ar.edu.itba.paw.persistence.SubcategoryDao;
import ar.edu.itba.paw.service.dto.ModelOption;
import ar.edu.itba.paw.service.exception.BadParameterException;
import ar.edu.itba.paw.service.exception.NotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CatalogServiceImpl implements CatalogService {

    private final CategoryDao categoryDao;
    private final SubcategoryDao subcategoryDao;
    private final BrandDao brandDao;
    private final ModelDao modelDao;
    private final ListingDao listingDao;

    @Override
    public Page<BrandInfo> getBrands(BrandSort sort, Integer page, int pageSize) {
        final int safePage = page == null || page < 1 ? 1 : page;
        return brandDao.getWithStats(sort == null ? BrandSort.BRAND_ASC : sort, safePage, pageSize);
    }

    @Override
    public List<Brand> getAllBrands() {
        return brandDao.getAll();
    }

    @Override
    public Map<Long, List<Brand>> getBrandReplacementOptions() {
        final List<Brand> brands = brandDao.getAll();
        final Map<Long, List<Brand>> options = new HashMap<>();
        for (final Brand brand : brands) {
            options.put(brand.getId(), brands.stream()
                .filter(candidate -> !candidate.getId().equals(brand.getId()))
                .toList());
        }
        return options;
    }

    @Override
    @Transactional
    public void replaceBrand(Long id, Long replacementId) {
        if (id == null || replacementId == null || id.equals(replacementId)) {
            throw BadParameterException.create("replacementBrandId", "Replacement brand must be different");
        }
        brandDao.getById(id)
            .orElseThrow(() -> NotFoundException.createFor("Brand with ID " + id));
        brandDao.getById(replacementId)
            .orElseThrow(() -> NotFoundException.createFor("Brand with ID " + replacementId));
        modelDao.replaceBrand(id, replacementId);
    }

    @Override
    @Transactional
    public Brand createBrand(String name) {
        final String trimmedName = name == null ? "" : name.trim();
        if (trimmedName.isEmpty()) {
            throw BadParameterException.create("name", "Brand name is required");
        }
        if (brandDao.getByName(trimmedName).isPresent()) {
            throw BadParameterException.create("name", "Brand name already exists");
        }
        return brandDao.create(trimmedName);
    }

    @Override
    @Transactional
    public void renameBrand(Long id, String name) {
        final String trimmedName = name == null ? "" : name.trim();
        if (trimmedName.isEmpty()) {
            throw BadParameterException.create("name", "Brand name is required");
        }
        if (brandDao.existsByNameAndIdNot(trimmedName, id)) {
            throw BadParameterException.create("name", "Brand name already exists");
        }
        brandDao.rename(id, trimmedName)
            .orElseThrow(() -> NotFoundException.createFor("Brand with ID " + id));
    }

    @Override
    @Transactional
    public void deleteBrand(Long id) {
        if (brandDao.hasListings(id)) {
            throw BadParameterException.create("brand", "Brand has listings");
        }
        if (!brandDao.delete(id)) {
            throw NotFoundException.createFor("Brand with ID " + id);
        }
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryDao.getAll();
    }

    @Override
    public List<Subcategory> getSubcategoriesByCategory(Long categoryId) {
        return subcategoryDao.getByCategoryId(categoryId);
    }

    @Override
    public List<Subcategory> getAllSubcategories() {
        return subcategoryDao.getAll();
    }

    @Override
    public List<Brand> getBrandsBySubcategory(Long subcategoryId) {
        return brandDao.getBySubcategoryId(subcategoryId);
    }

    @Override
    public List<Model> getModelsBySubcategoryAndBrand(Long subcategoryId, Long brandId) {
        return modelDao.getBySubcategoryIdAndBrandId(subcategoryId, brandId);
    }

    @Override
    public Model getModelById(Long id) {
        return modelDao
            .getById(id)
            .orElseThrow(() -> NotFoundException.createFor("Model with ID " + id));
    }

    @Override
    public Page<ModelInfo> getModels(Long brandId, Long subcategoryId, ModelSort sort, Integer page, int pageSize) {
        final int safePage = page == null || page < 1 ? 1 : page;
        return modelDao.getWithInfo(
            brandId, subcategoryId, sort == null ? ModelSort.NAME_ASC : sort, safePage, pageSize
        );
    }

    @Override
    public List<Model> getAllModels() {
        return modelDao.getAllWithBrand();
    }

    @Override
    public Map<Long, List<ModelOption>> getModelReplacementOptions() {
        final List<Model> models = modelDao.getAllWithBrand();
        final Map<Long, List<ModelOption>> options = new HashMap<>();
        for (final Model model : models) {
            options.put(model.getId(), models.stream()
                .filter(candidate -> !candidate.getId().equals(model.getId()))
                .map(candidate -> ModelOption.builder()
                    .id(candidate.getId())
                    .label(candidate.getBrand().getName() + " — " + candidate.getName())
                    .build())
                .toList());
        }
        return options;
    }

    @Override
    @Transactional
    public Model createModel(String name, Long brandId, Long subcategoryId) {
        final String trimmedName = name == null ? "" : name.trim();
        if (trimmedName.isEmpty()) {
            throw BadParameterException.create("name", "Model name is required");
        }
        final Brand brand = brandDao.getById(brandId)
            .orElseThrow(() -> NotFoundException.createFor("Brand with ID " + brandId));
        final Subcategory subcategory = subcategoryDao.getById(subcategoryId)
            .orElseThrow(() -> NotFoundException.createFor("Subcategory with ID " + subcategoryId));
        if (modelDao.existsByNameAndBrandId(trimmedName, brandId)) {
            throw BadParameterException.create("name", "Model name already exists for this brand");
        }
        return modelDao.create(trimmedName, brand, subcategory);
    }

    @Override
    @Transactional
    public void updateModel(Long id, String name, Long brandId, Long subcategoryId) {
        final String trimmedName = name == null ? "" : name.trim();
        if (trimmedName.isEmpty()) {
            throw BadParameterException.create("name", "Model name is required");
        }
        final Brand brand = brandDao.getById(brandId)
            .orElseThrow(() -> NotFoundException.createFor("Brand with ID " + brandId));
        final Subcategory subcategory = subcategoryDao.getById(subcategoryId)
            .orElseThrow(() -> NotFoundException.createFor("Subcategory with ID " + subcategoryId));
        if (modelDao.existsByNameAndBrandIdAndIdNot(trimmedName, brandId, id)) {
            throw BadParameterException.create("name", "Model name already exists for this brand");
        }
        modelDao.update(id, trimmedName, brand, subcategory)
            .orElseThrow(() -> NotFoundException.createFor("Model with ID " + id));
    }

    @Override
    @Transactional
    public void deleteModel(Long id) {
        if (modelDao.hasListings(id)) {
            throw BadParameterException.create("model", "Model has listings");
        }
        if (!modelDao.deleteModel(id)) {
            throw NotFoundException.createFor("Model with ID " + id);
        }
    }

    @Override
    @Transactional
    public void replaceModel(Long id, Long replacementId) {
        if (id == null || replacementId == null || id.equals(replacementId)) {
            throw BadParameterException.create("replacementModelId", "Replacement model must be different");
        }
        modelDao.getById(id)
            .orElseThrow(() -> NotFoundException.createFor("Model with ID " + id));
        modelDao.getById(replacementId)
            .orElseThrow(() -> NotFoundException.createFor("Model with ID " + replacementId));
        listingDao.replaceModel(id, replacementId);
    }

    @Override
    public Page<ModelListingInfoSummary> getListingInfoSummary(Long brandId, Long modelId, Integer fromYear,
            Integer toYear, @NonNull Long subcategoryId, Integer page, int pageSize) {
        final int safePage = page == null || page < 1 ? 1 : page;
        final var infoPage = modelDao.getListingInfoSummary(
            brandId, modelId, fromYear, toYear, subcategoryId, safePage, pageSize
        );

        final Map<Long, List<ModelListingInfo>> infoByModelId = new LinkedHashMap<>();
        for (final ModelListingInfo info : infoPage.getContent()) {
            infoByModelId
                .computeIfAbsent(info.getModel().getId(), key -> new ArrayList<>())
                .add(info);
        }

        final var summaries = infoByModelId.values()
            .stream()
            .map(values -> ModelListingInfoSummary.builder()
                .brand(values.get(0).getBrand())
                .model(values.get(0).getModel())
                .listingInfoByCondition(values.stream()
                    .sorted(Comparator.comparingInt(info -> info.getCondition().ordinal()))
                    .toList())
                .build()
            )
            .toList();

        return new Page<>(summaries, infoPage.getPage(), infoPage.getPageSize(), infoPage.getTotalCount());
    }
}
