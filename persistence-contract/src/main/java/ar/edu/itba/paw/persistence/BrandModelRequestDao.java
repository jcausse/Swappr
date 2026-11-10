package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.BrandModelRequest;
import ar.edu.itba.paw.model.BrandModelRequestType;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Subcategory;
import ar.edu.itba.paw.model.User;
import java.util.Optional;

public interface BrandModelRequestDao {
    Optional<BrandModelRequest> getById(Long id);

    Page<BrandModelRequest> getMatching(int page, int pageSize);

    BrandModelRequest create(
            BrandModelRequestType type,
            Brand brand,
            String brandName,
            String modelName,
            Subcategory subcategory,
            User requester
    );

    boolean delete(Long id);
}
