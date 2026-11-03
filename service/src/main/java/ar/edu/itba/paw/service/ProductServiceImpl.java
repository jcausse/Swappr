package ar.edu.itba.paw.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.edu.itba.paw.model.Category;
import ar.edu.itba.paw.model.Product;
import ar.edu.itba.paw.model.ProductListingInfo;
import ar.edu.itba.paw.model.ProductListingInfoSummary;
import ar.edu.itba.paw.model.Subcategory;
import ar.edu.itba.paw.persistence.CategoryDao;
import ar.edu.itba.paw.persistence.ProductDao;
import ar.edu.itba.paw.persistence.SubcategoryDao;
import ar.edu.itba.paw.service.exception.NotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductDao productDao;
    private final CategoryDao categoryDao;
    private final SubcategoryDao subcategoryDao;

    @Override
    public Product getById(Long id) {
        return productDao
            .getById(id)
            .orElseThrow(() ->
                NotFoundException.createFor("Product with ID " + id)
            );
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
    public List<String> getBrandsBySubcategory(Long subcategoryId) {
        return productDao.getBrandsBySubcategory(subcategoryId);
    }

    @Override
    public List<String> getModelsBySubcategoryAndBrand(Long subcategoryId, String brand) {
        return productDao.getModelsBySubcategoryAndBrand(subcategoryId, brand);
    }

    @Override
    @Transactional
    public Product findOrCreateByBrandModelYear(
            @NonNull String brand,
            @NonNull String model,
            @NonNull Integer year,
            @NonNull Long subcategoryId
    ) {
        LOGGER.debug("Finding or creating product: brand='{}', model='{}', year={}, subcategoryId={}",
                brand, model, year, subcategoryId);

        var subcategory = subcategoryDao.getById(subcategoryId)
            .orElseThrow(() -> NotFoundException.createFor("Subcategory with ID " + subcategoryId));

        return productDao.getByBrandModelYearSubcategory(brand, model, year, subcategoryId)
            .map(product -> {
                LOGGER.debug("Found existing product with id={}", product.getId());
                return product;
            })
            .orElseGet(() -> {
                var created = productDao.create(brand, model, year, subcategory);
                LOGGER.info("Created new product: id={}, brand='{}', model='{}', year={}",
                        created.getId(), brand, model, year);
                return created;
            });
    }

    @Override
    @Transactional
    public List<ProductListingInfoSummary> getListingInfoSummary(String brand, String model, Integer fromYear, Integer toYear, @NonNull Long subcategoryId) {
        var info = productDao.getListingInfoSummary(brand, model, fromYear, toYear, subcategoryId);

        Map<String, List<ProductListingInfo>> infoByBrandModel = new HashMap<>();
        for (var i : info) {
            var brandAndModel = i.getBrand() + " " + i.getModel();
            infoByBrandModel.putIfAbsent(brandAndModel, new ArrayList<>());
            infoByBrandModel.get(brandAndModel).add(i);
        }

        return infoByBrandModel.entrySet()
                .stream()
                .map((entry) -> ProductListingInfoSummary.builder()
                    .brand(entry.getValue().get(0).getBrand())
                    .model(entry.getValue().get(0).getModel())
                    .listingInfoByCondition(entry.getValue())
                    .build()
                )
                .toList();
    }
}
