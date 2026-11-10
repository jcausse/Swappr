package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.BrandModelRequest;
import ar.edu.itba.paw.model.BrandModelRequestType;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.BrandModelRequestDao;
import ar.edu.itba.paw.service.dto.BrandModelRequestAcceptanceDto;
import ar.edu.itba.paw.service.dto.BrandModelRequestCreationDto;
import ar.edu.itba.paw.service.exception.BadParameterException;
import ar.edu.itba.paw.service.exception.BrandModelRequestAlreadyExistsException;
import ar.edu.itba.paw.service.exception.ModelAlreadyExistsException;
import ar.edu.itba.paw.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

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

        BrandModelRequestType type = dto.type();
        Brand brand = null;
        String brandName = null;
        if (type == BrandModelRequestType.EXISTING_BRAND) {
            if (dto.brandId() == null) {
                throw BadParameterException.create("brandId", "Brand is required");
            }
            brand = catalogService.getBrandById(dto.brandId());
        } else {
            brandName = normalized(dto.newBrandName());
            if (brandName.isEmpty()) {
                throw BadParameterException.create("newBrandName", "Brand name is required");
            }
            final var existingBrand = catalogService.findBrandByName(brandName);
            if (existingBrand.isPresent()) {
                brand = existingBrand.get();
                brandName = null;
                type = BrandModelRequestType.EXISTING_BRAND;
            }
        }

        if (brand != null) {
            final var existingModel = catalogService.findModelByNameAndBrand(modelName, brand.getId());
            if (existingModel.isPresent()) {
                throw ModelAlreadyExistsException.createFor(existingModel.get());
            }
        }

        final String matchingBrandName = brand != null ? brand.getName() : brandName;
        final var existingRequest = brandModelRequestDao.findMatching(
            matchingBrandName, modelName, subcategory.getId()
        );
        if (existingRequest.isPresent()) {
            final BrandModelRequest request = existingRequest.get();
            if (request.getRequester().getId().equals(requester.getId())
                    || brandModelRequestDao.isAdditionalRequester(request.getId(), requester.getId())) {
                throw new BrandModelRequestAlreadyExistsException(dto);
            }
            brandModelRequestDao.addAdditionalRequester(request.getId(), requester.getId());
            return request;
        }

        return brandModelRequestDao.create(type, brand, brandName, modelName, subcategory, requester);
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

        final List<User> recipients = requestersOf(request);
        brandModelRequestDao.delete(requestId);
        notificationDispatcherService.notifyBrandModelRequestAccepted(request, recipients, brand, model);
    }

    @Override
    @Transactional
    public void reject(Long requestId) {
        final var request = brandModelRequestDao.getById(requestId)
            .orElseThrow(() -> NotFoundException.createFor("Brand model request with ID " + requestId));
        final List<User> recipients = requestersOf(request);
        brandModelRequestDao.delete(requestId);
        notificationDispatcherService.notifyBrandModelRequestRejected(request, recipients);
    }

    private static List<User> requestersOf(BrandModelRequest request) {
        return Stream.concat(
                Stream.of(request.getRequester()),
                request.getAdditionalRequesters().stream()
            )
            .distinct()
            .toList();
    }

    private static String normalized(String value) {
        return value == null ? "" : value.trim();
    }
}
