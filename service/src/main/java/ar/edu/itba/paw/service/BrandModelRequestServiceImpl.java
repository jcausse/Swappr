package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.BrandModelRequest;
import ar.edu.itba.paw.model.BrandModelRequestType;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.persistence.BrandModelRequestDao;
import ar.edu.itba.paw.service.dto.BrandModelRequestAcceptanceDto;
import ar.edu.itba.paw.service.dto.BrandModelRequestCreationDto;
import ar.edu.itba.paw.service.exception.BadParameterException;
import ar.edu.itba.paw.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BrandModelRequestServiceImpl implements BrandModelRequestService {

    private final BrandModelRequestDao brandModelRequestDao;
    private final CatalogService catalogService;
    private final UserService userService;
    private final NotificationDispatcherService notificationDispatcherService;

    @Override
    public Page<BrandModelRequest> getRequests(int page, int pageSize) {
        final int safePage = page < 1 ? 1 : page;
        return brandModelRequestDao.getMatching(safePage, pageSize);
    }

    @Override
    @Transactional
    public BrandModelRequest create(BrandModelRequestCreationDto dto) {
        if (dto.type() == null) {
            throw BadParameterException.create("type", "Request type is required");
        }
        final String modelName = normalized(dto.modelName());
        if (modelName.isEmpty()) {
            throw BadParameterException.create("modelName", "Model name is required");
        }
        if (dto.subcategoryId() == null) {
            throw BadParameterException.create("subcategoryId", "Subcategory is required");
        }
        final var subcategory = catalogService.getSubcategoryById(dto.subcategoryId());
        final var requester = userService.getById(dto.requesterId())
            .orElseThrow(() -> NotFoundException.createFor("User with ID " + dto.requesterId()));

        Brand brand = null;
        String brandName = null;
        if (dto.type() == BrandModelRequestType.EXISTING_BRAND) {
            if (dto.brandId() == null) {
                throw BadParameterException.create("brandId", "Brand is required");
            }
            brand = catalogService.getBrandById(dto.brandId());
        } else {
            brandName = normalized(dto.newBrandName());
            if (brandName.isEmpty()) {
                throw BadParameterException.create("newBrandName", "Brand name is required");
            }
        }

        return brandModelRequestDao.create(dto.type(), brand, brandName, modelName, subcategory, requester);
    }

    @Override
    @Transactional
    public void accept(Long requestId, BrandModelRequestAcceptanceDto dto) {
        final var request = brandModelRequestDao.getById(requestId)
            .orElseThrow(() -> NotFoundException.createFor("Brand model request with ID " + requestId));

        if (dto.type() == null) {
            throw BadParameterException.create("type", "Request type is required");
        }
        final String modelName = normalized(dto.modelName());
        if (modelName.isEmpty()) {
            throw BadParameterException.create("modelName", "Model name is required");
        }

        final Brand brand;
        if (dto.type() == BrandModelRequestType.NEW_BRAND) {
            final String brandName = normalized(dto.newBrandName());
            if (brandName.isEmpty()) {
                throw BadParameterException.create("newBrandName", "Brand name is required");
            }
            brand = catalogService.createBrand(brandName);
        } else {
            if (dto.brandId() == null) {
                throw BadParameterException.create("brandId", "Brand is required");
            }
            brand = catalogService.getBrandById(dto.brandId());
        }

        final var model = catalogService.createModel(
            modelName,
            brand.getId(),
            request.getSubcategory().getId()
        );

        brandModelRequestDao.delete(requestId);
        notificationDispatcherService.notifyBrandModelRequestAccepted(request, brand, model);
    }

    @Override
    @Transactional
    public void reject(Long requestId) {
        final var request = brandModelRequestDao.getById(requestId)
            .orElseThrow(() -> NotFoundException.createFor("Brand model request with ID " + requestId));
        brandModelRequestDao.delete(requestId);
        notificationDispatcherService.notifyBrandModelRequestRejected(request);
    }

    private static String normalized(String value) {
        return value == null ? "" : value.trim();
    }
}
