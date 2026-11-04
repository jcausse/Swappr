package ar.edu.itba.paw.webapp.controller;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import ar.edu.itba.paw.model.File;
import ar.edu.itba.paw.model.ListingSort;
import ar.edu.itba.paw.model.ListingStatus;
import ar.edu.itba.paw.model.Offer;
import ar.edu.itba.paw.model.OfferStatusGroup;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.ListingService;
import ar.edu.itba.paw.service.OfferService;
import ar.edu.itba.paw.service.dto.ListingFilterDto;
import ar.edu.itba.paw.service.dto.OfferFilterDto;
import ar.edu.itba.paw.service.exception.BadParameterException;
import ar.edu.itba.paw.service.exception.NotFoundException;
import ar.edu.itba.paw.webapp.auth.CurrentUser;
import ar.edu.itba.paw.webapp.form.ListingFilterForm;
import ar.edu.itba.paw.webapp.form.OfferFilterForm;
import ar.edu.itba.paw.webapp.form.ProofOfPaymentUploadForm;
import ar.edu.itba.paw.webapp.form.ProofOfShippingUploadForm;
import ar.edu.itba.paw.webapp.form.RateForm;
import ar.edu.itba.paw.webapp.form.StringSelectOption;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/account")
public class AccountController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountController.class);

    private static final int ACCOUNT_LISTINGS_PAGE_SIZE = 5;

    private final ListingService listingService;
    private final OfferService offerService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ModelAndView index(@CurrentUser User currentUser) {
        LOGGER.debug("User {} accessing account index", currentUser.getId());
		return new ModelAndView("account/index")
                .addObject("pendingOffersCount", offerService.getPendingOffersCount(currentUser));
    }

    @GetMapping("/listings")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView listings(@CurrentUser User currentUser, @ModelAttribute("filterForm") ListingFilterForm filterForm) {
        LOGGER.debug("User {} accessing account listings", currentUser.getId());

        final var filter = ListingFilterDto.builder()
            .query(filterForm.getQuery())
            .sort(filterForm.getSort())
            .creatorId(currentUser.getId())
            .status(filterForm.getStatus())
            .page(filterForm.getPage())
            .pageSize(ACCOUNT_LISTINGS_PAGE_SIZE)
            .build();

        final var listingPage = listingService.search(filter);
        final var listings = listingPage.getContent();

        final var statusOptions = Arrays.stream(ListingStatus.values())
            .map(s -> new StringSelectOption(s.name(), "listing.status." + s.name()))
            .toList();

        final var sortOptions = Arrays.stream(ListingSort.values())
            .map(s -> new StringSelectOption(s.name(), "listing.sort." + s.getKey()))
            .toList();

		return new ModelAndView("account/listings")
                .addObject("listingPage", listingPage)
                .addObject("listings", listings)
                .addObject("statusOptions", statusOptions)
                .addObject("sortOptions", sortOptions)
                .addObject("pendingOffersCount", offerService.getPendingOffersCount(currentUser));
    }

    @GetMapping("/incoming-offers")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView incomingOffers(@CurrentUser User currentUser, @ModelAttribute("filterForm") OfferFilterForm filterForm) {
        LOGGER.debug("User {} accessing incoming offers", currentUser.getId());

        final var filter = OfferFilterDto.builder()
            .sellerId(currentUser.getId())
            .statusGroup(filterForm.getStatusGroup())
            .page(filterForm.getPage())
            .pageSize(5)
            .build();
        final var offerPage = offerService.get(filter);

        final var statusGroupOptions = Arrays.stream(OfferStatusGroup.values())
            .map(s -> new StringSelectOption(s.name(), "offer.statusGroup." + s.getStatus()))
            .toList();

		return new ModelAndView("account/incomingOffers")
                .addObject("offerPage", offerPage)
                .addObject("offers", offerPage.getContent())
                .addObject("statusGroupOptions", statusGroupOptions)
                .addObject("offerCountByStatus", getOfferCountByStatus(currentUser, true))
                .addObject("pendingOffersCount", offerService.getPendingOffersCount(currentUser));
    }

    @GetMapping("/my-offers")
    @PreAuthorize("isAuthenticated()")
    public ModelAndView myOffers(@CurrentUser User currentUser, @ModelAttribute("filterForm") OfferFilterForm filterForm) {
        LOGGER.debug("User {} accessing my offers", currentUser.getId());

        final var filter = OfferFilterDto.builder()
            .buyerId(currentUser.getId())
            .statusGroup(filterForm.getStatusGroup())
            .page(filterForm.getPage())
            .pageSize(5)
            .build();
        final var offerPage = offerService.get(filter);

        final var statusGroupOptions = Arrays.stream(OfferStatusGroup.values())
            .map(s -> new StringSelectOption(s.name(), "offer.statusGroup." + s.getStatus()))
            .toList();

		return new ModelAndView("account/myOffers")
                .addObject("offerPage", offerPage)
                .addObject("offers", offerPage.getContent())
                .addObject("statusGroupOptions", statusGroupOptions)
                .addObject("offerCountByStatus", getOfferCountByStatus(currentUser, false))
                .addObject("pendingOffersCount", offerService.getPendingOffersCount(currentUser));
    }

    private Map<String, Long> getOfferCountByStatus(final User user, final boolean incoming) {
        return offerService.getOfferCountByStatus(user, incoming).entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));
    }

	@GetMapping("/incoming-offers/{offerId}")
    @PreAuthorize("@webPermissionService.canManageOfferAsSeller(authentication, #offerId)")
    public ModelAndView incomingOfferDetail(@PathVariable Long offerId, @CurrentUser User currentUser) {
        LOGGER.debug("User {} viewing incoming offer {}", currentUser.getId(), offerId);

		return new ModelAndView("account/incomingOfferDetail")
                .addObject("offer", offerService.getById(offerId).orElseThrow(() -> NotFoundException.createFor("Offer")))
                .addObject("pendingOffersCount", offerService.getPendingOffersCount(currentUser));
    }

    @GetMapping("/my-offers/{offerId}/payment")
    @PreAuthorize("@webPermissionService.canManageOfferAsBuyer(authentication, #offerId)")
    public ModelAndView showProofOfPaymentUpload(@PathVariable Long offerId, @CurrentUser User currentUser) {
        LOGGER.debug("User {} accessing proof of payment upload for offer {}", currentUser.getId(), offerId);
        final Offer offer = offerService.getById(offerId)
                .orElseThrow(() -> NotFoundException.createFor("Offer"));

        if (!offer.canUploadDocuments()) {
            throw new BadParameterException("Offer does not accept uploading documents in current state");
        }

        return new ModelAndView("account/proofOfPaymentUpload")
            .addObject("offer", offer)
            .addObject("proofOfPaymentUploadForm", new ProofOfPaymentUploadForm())
		    .addObject("pendingOffersCount", offerService.getPendingOffersCount(currentUser));
    }

    @PostMapping("/my-offers/{offerId}/payment")
    @PreAuthorize("@webPermissionService.canManageOfferAsBuyer(authentication, #offerId)")
    public ModelAndView uploadProofOfPayment(@PathVariable Long offerId,
                                              @CurrentUser User currentUser,
                                              @Valid @ModelAttribute("proofOfPaymentUploadForm") ProofOfPaymentUploadForm form,
                                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            LOGGER.debug("Validation failed for proof of payment upload on offer {}", offerId);
            return new ModelAndView("account/proofOfPaymentUpload")
                .addObject("offer", offerService.getById(offerId).orElseThrow(() -> NotFoundException.createFor("Offer")))
                .addObject("pendingOffersCount", offerService.getPendingOffersCount(currentUser));
        }

        offerService.uploadProofOfPayment(
                offerId,
                currentUser.getId(),
                form.getFileDto()
        );
        LOGGER.info("User {} uploaded proof of payment for offer {}", currentUser.getId(), offerId);

        return new ModelAndView("redirect:/account/my-offers?statusGroup=PENDING_PAYMENT");
    }

    @GetMapping("/my-offers/{offerId}/shipping")
    @PreAuthorize("@webPermissionService.canManageOfferAsSeller(authentication, #offerId)")
    public ModelAndView showProofOfShippingUpload(@PathVariable Long offerId, @CurrentUser User currentUser) {
        LOGGER.debug("User {} accessing proof of shipping upload for offer {}", currentUser.getId(), offerId);
        final Offer offer = offerService.getById(offerId)
                .orElseThrow(() -> NotFoundException.createFor("Offer"));

        if (!offer.canUploadDocuments()) {
            throw new BadParameterException("Offer does not accept uploading documents in current state");
        }

        return new ModelAndView("account/proofOfShippingUpload")
            .addObject("offer", offer)
            .addObject("proofOfShippingUploadForm", new ProofOfShippingUploadForm())
            .addObject("pendingOffersCount", offerService.getPendingOffersCount(currentUser));
    }

    @PostMapping("/my-offers/{offerId}/shipping")
    @PreAuthorize("@webPermissionService.canManageOfferAsSeller(authentication, #offerId)")
    public ModelAndView uploadProofOfShipping(@PathVariable Long offerId,
                                               @CurrentUser User currentUser,
                                               @Valid @ModelAttribute("proofOfShippingUploadForm") ProofOfShippingUploadForm form,
                                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            LOGGER.debug("Validation failed for proof of shipping upload on offer {}", offerId);
            return new ModelAndView("account/proofOfShippingUpload")
                .addObject("offer", offerService.getById(offerId).orElseThrow(() -> NotFoundException.createFor("Offer")))
                .addObject("pendingOffersCount", offerService.getPendingOffersCount(currentUser));
        }

        offerService.uploadProofOfShipping(
                offerId,
                currentUser.getId(),
                form.getFileDto(),
                form.getTrackingNumber()
        );
        LOGGER.info("User {} uploaded proof of shipping for offer {}", currentUser.getId(), offerId);

        return new ModelAndView("redirect:/account/incoming-offers?statusGroup=PENDING_PAYMENT");
    }

    @GetMapping("/my-offers/{offerId}/payment/download")
    @PreAuthorize("@webPermissionService.canAccessOffer(authentication, #offerId)")
    public ResponseEntity<Resource> downloadProofOfPayment(@PathVariable Long offerId, @CurrentUser User currentUser) {
        LOGGER.debug("User {} downloading proof of payment for offer {}", currentUser.getId(), offerId);
        final Long currentUserId = currentUser.getId();
        final File file = offerService.getProofOfPaymentFile(offerId, currentUserId)
                .orElseThrow(() -> NotFoundException.createFor("Proof of payment not found"));

        return getResource(file);
    }

    @GetMapping("/my-offers/{offerId}/shipping/download")
    @PreAuthorize("@webPermissionService.canAccessOffer(authentication, #offerId)")
    public ResponseEntity<Resource> downloadProofOfShipping(@PathVariable Long offerId, @CurrentUser User currentUser) {
        LOGGER.debug("User {} downloading proof of shipping for offer {}", currentUser.getId(), offerId);
        final Long currentUserId = currentUser.getId();
        final File file = offerService.getProofOfShippingFile(offerId, currentUserId)
                .orElseThrow(() -> NotFoundException.createFor("Proof of shipping not found"));

        return getResource(file);
    }

	private ResponseEntity<Resource> getResource(final File file) {
		final ByteArrayResource resource = new ByteArrayResource(file.getData());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType().orElse("application/octet-stream")))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .contentLength(file.getData().length)
                .body(resource);
	}

    @GetMapping("/rate/{offerId}")
    @PreAuthorize("@webPermissionService.canAccessOffer(authentication, #offerId)")
    public ModelAndView showRateForm(@PathVariable Long offerId, @CurrentUser User currentUser,
                                      @ModelAttribute("rateForm") RateForm rateForm) {
        final Offer offer = offerService.getById(offerId)
                .orElseThrow(() -> NotFoundException.createFor("Offer"));

        if (!offer.canBeRatedBy(currentUser)) {
            throw new BadParameterException("Only accepted offers can be rated");
        }

        return new ModelAndView("account/rate")
            .addObject("offer", offer)
    		.addObject("pendingOffersCount", offerService.getPendingOffersCount(currentUser));
    }

    @PostMapping("/rate/{offerId}")
    @PreAuthorize("@webPermissionService.canAccessOffer(authentication, #offerId)")
    public ModelAndView submitRateForm(@PathVariable Long offerId,
                                        @CurrentUser User currentUser,
                                        @Valid @ModelAttribute("rateForm") RateForm rateForm,
                                        BindingResult bindingResult) {
        final Offer offer = offerService.getById(offerId)
                .orElseThrow(() -> NotFoundException.createFor("Offer")); // podria moverse a la primera linea de oferservice.rate

        if (bindingResult.hasErrors()) {
            return new ModelAndView("account/rate")
                .addObject("offer", offer)
    			.addObject("pendingOffersCount", offerService.getPendingOffersCount(currentUser));
        }

        offerService.rate(offer, currentUser, rateForm.getRating(), rateForm.getReviewText());

        return new ModelAndView("redirect:" + (offer.isBuyer(currentUser)
            ? "/account/my-offers?statusGroup=RESOLVED"
            : "/account/incoming-offers?statusGroup=RESOLVED"));
    }
}
