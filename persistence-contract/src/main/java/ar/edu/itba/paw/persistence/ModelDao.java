package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Model;
import ar.edu.itba.paw.model.ModelListingInfo;
import ar.edu.itba.paw.model.Page;
import java.util.List;
import java.util.Optional;

public interface ModelDao {
    Optional<Model> getById(Long id);

    List<Model> getBySubcategoryIdAndBrandId(Long subcategoryId, Long brandId);

    Page<ModelListingInfo> getListingInfoSummary(Long brandId, Long modelId, Integer fromYear, Integer toYear,
            Long subcategoryId, int page, int pageSize);
}
