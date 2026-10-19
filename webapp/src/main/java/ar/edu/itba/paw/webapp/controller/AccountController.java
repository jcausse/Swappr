package ar.edu.itba.paw.webapp.controller;

import java.util.Arrays;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import ar.edu.itba.paw.model.File;
import ar.edu.itba.paw.model.Listing;
import ar.edu.itba.paw.model.ListingSort;
import ar.edu.itba.paw.model.ListingStatus;
import ar.edu.itba.paw.model.Offer;
import ar.edu.itba.paw.model.OfferStatus;
import ar.edu.itba.paw.model.OfferStatusGroup;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.service.ListingService;
import ar.edu.itba.paw.service.OfferService;
import ar.edu.itba.paw.service.dto.ListingFilterDto;
import ar.edu.itba.paw.service.dto.OfferFilterDto;
import ar.edu.itba.paw.service.exception.BadParameterException;
import ar.edu.itba.paw.service.exception.ForbiddenException;
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
    private final MessageSource messageSource;

    @GetMapping
    public ModelAndView index(@CurrentUser User currentUser) {
        LOGGER.debug("User {} accessing account index", currentUser.getId());
        return new ModelAndView("account/index")
                .addObject("pendingOffersCount", getPendingOffersCount(currentUser));
    }

    @GetMapping("/listings")
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

        final var locale = LocaleContextHolder.getLocale();
        final var statusOptions = Arrays.stream(ListingStatus.values())
            .map(s -> new StringSelectOption(s.name(), messageSource.getMessage("listing.status." + s.name(), null, locale)))
            .toList();

        final var sortOptions = Arrays.stream(ListingSort.values())
            .map(s -> {
                String key;
                if (s == ListingSort.RECENT) key = "discovery.sort.recent";
                else if (s == ListingSort.PRICE_ASC) key = "discovery.sort.price_asc";
                else if (s == ListingSort.PRICE_DESC) key = "discovery.sort.price_desc";
                else if (s == ListingSort.NAME_ASC) key = "account.listings.sort.name_asc";
                else if (s == ListingSort.NAME_DESC) key = "account.listings.sort.name_desc";
                else key = s.getKey();
                return new StringSelectOption(s.getKey(), messageSource.getMessage(key, null, locale));
            })
            .toList();

        return new ModelAndView("account/listings")
                .addObject("listingPage", listingPage)
                .addObject("listings", listings)
                .addObject("statusOptions", statusOptions)
                .addObject("sortOptions", sortOptions)
                .addObject("pendingOffersCount", getPendingOffersCount(currentUser));
    }

    @GetMapping("/incoming-offers")
    public ModelAndView incomingOffers(@CurrentUser User currentUser, @ModelAttribute("filterForm") OfferFilterForm filterForm) {
        LOGGER.debug("User {} accessing incoming offers", currentUser.getId());
        if (filterForm.getStatusGroup() == null || filterForm.getStatusGroup().isBlank()) {
            filterForm.setStatusGroup(OfferStatusGroup.PENDING.getStatus());
        }

        final var filter = OfferFilterDto.builder()
            .sellerId(currentUser.getId())
            .statusGroup(filterForm.getStatusGroup())
            .page(filterForm.getPage())
            .pageSize(5)
            .build();
        final var offerPage = offerService.get(filter);

        final var locale = LocaleContextHolder.getLocale();
        final var statusGroupOptions = Arrays.stream(OfferStatusGroup.values())
            .map(s -> new StringSelectOption(s.getStatus(), messageSource.getMessage("offer.statusGroup." + s.getStatus(), null, locale)))
            .toList();

        return new ModelAndView("account/incomingOffers")
                .addObject("offerPage", offerPage)
                .addObject("offers", offerPage.getContent())
                .addObject("statusGroupOptions", statusGroupOptions)
                .addObject("pendingOffersCount", getPendingOffersCount(currentUser));
    }

    @GetMapping("/my-offers")
    public ModelAndView myOffers(@CurrentUser User currentUser, @ModelAttribute("filterForm") OfferFilterForm filterForm) {
        LOGGER.debug("User {} accessing my offers", currentUser.getId());
        if (filterForm.getStatusGroup() == null || filterForm.getStatusGroup().isBlank()) {
            filterForm.setStatusGroup(OfferStatusGroup.PENDING.getStatus());
        }

        final var filter = OfferFilterDto.builder()
            .buyerId(currentUser.getId())
            .statusGroup(filterForm.getStatusGroup())
            .page(filterForm.getPage())
            .pageSize(5)
            .build();
        final var offerPage = offerService.get(filter);

        final var locale = LocaleContextHolder.getLocale();
        final var statusGroupOptions = Arrays.stream(OfferStatusGroup.values())
            .map(s -> new StringSelectOption(s.getStatus(), messageSource.getMessage("offer.statusGroup." + s.getStatus(), null, locale)))
            .toList();

        return new ModelAndView("account/myOffers")
                .addObject("offerPage", offerPage)
                .addObject("offers", offerPage.getContent())
                .addObject("statusGroupOptions", statusGroupOptions)
                .addObject("pendingOffersCount", getPendingOffersCount(currentUser));
    }

	private int getPendingOffersCount(final User user) {
		return offerService.getPendingOffersCount(user);
	}

    // Offer detail page - seller view
    @GetMapping("/incoming-offers/{offerId}")
    public ModelAndView incomingOfferDetail(@PathVariable Long offerId, @CurrentUser User currentUser) {
        LOGGER.debug("User {} viewing incoming offer {}", currentUser.getId(), offerId);
        final Offer offer = offerService.getById(offerId)
                .orElseThrow(() -> NotFoundException.createFor("Offer"));

        final Listing listing = offer.getListing();
        final Long currentUserId = currentUser.getId();

        // Verify the current user is the seller (listing creator)
        // FIXME use preauthorize
        if (!listing.getCreator().getId().equals(currentUserId)) {
            throw new ForbiddenException("Not authorized to view this offer");
        }

        var mav = new ModelAndView("account/incomingOfferDetail");
        mav.addObject("offer", offer);
        mav.addObject("pendingOffersCount", getPendingOffersCount(currentUser));
        return mav;
    }

    // Proof of payment upload - buyer
    @GetMapping("/my-offers/{offerId}/payment")
    public ModelAndView showProofOfPaymentUpload(@PathVariable Long offerId, @CurrentUser User currentUser) {
        LOGGER.debug("User {} accessing proof of payment upload for offer {}", currentUser.getId(), offerId);
        final Offer offer = offerService.getById(offerId)
                .orElseThrow(() -> NotFoundException.createFor("Offer"));

        // FIXME use preauthorize
        if (!offer.getBuyer().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Not authorized to upload proof of payment");
        }

        // TODO check this
        if (offer.getStatus() != OfferStatus.PENDING_PAYMENT) {
            throw new BadParameterException("Offer is not pending payment");
        }

        var mav = new ModelAndView("account/proofOfPaymentUpload");
        mav.addObject("offer", offer);
        mav.addObject("proofOfPaymentUploadForm", new ProofOfPaymentUploadForm());
        mav.addObject("pendingOffersCount", getPendingOffersCount(currentUser));
        return mav;
    }

    @PostMapping("/my-offers/{offerId}/payment")
    public ModelAndView uploadProofOfPayment(@PathVariable Long offerId,
                                              @CurrentUser User currentUser,
                                              @Valid @ModelAttribute("proofOfPaymentUploadForm") ProofOfPaymentUploadForm form,
                                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            LOGGER.debug("Validation failed for proof of payment upload on offer {}", offerId);
            var mav = new ModelAndView("account/proofOfPaymentUpload");
            mav.addObject("offer", offerService.getById(offerId).orElseThrow(() -> NotFoundException.createFor("Offer")));
            mav.addObject("pendingOffersCount", getPendingOffersCount(currentUser));
            return mav;
        }

        offerService.uploadProofOfPayment(
                offerId,
                currentUser.getId(),
                form.getFileDto()
        );
        LOGGER.info("User {} uploaded proof of payment for offer {}", currentUser.getId(), offerId);

        return new ModelAndView("redirect:/account/my-offers?statusGroup=pending_payment");
    }

    // Proof of shipping upload - seller
    @GetMapping("/my-offers/{offerId}/shipping")
    public ModelAndView showProofOfShippingUpload(@PathVariable Long offerId, @CurrentUser User currentUser) {
        LOGGER.debug("User {} accessing proof of shipping upload for offer {}", currentUser.getId(), offerId);
        final Offer offer = offerService.getById(offerId)
                .orElseThrow(() -> NotFoundException.createFor("Offer"));

        // FIXME use preauthorize
        if (!offer.getListing().getCreator().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Not authorized to upload proof of shipping");
        }

        // TODO check this
        if (offer.getStatus() != OfferStatus.PENDING_PAYMENT) {
            throw new BadParameterException("Offer is not pending payment");
        }

        var mav = new ModelAndView("account/proofOfShippingUpload");
        mav.addObject("offer", offer);
        mav.addObject("proofOfShippingUploadForm", new ProofOfShippingUploadForm());
        mav.addObject("pendingOffersCount", getPendingOffersCount(currentUser));
        return mav;
    }

    @PostMapping("/my-offers/{offerId}/shipping")
    public ModelAndView uploadProofOfShipping(@PathVariable Long offerId,
                                               @CurrentUser User currentUser,
                                               @Valid @ModelAttribute("proofOfShippingUploadForm") ProofOfShippingUploadForm form,
                                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            LOGGER.debug("Validation failed for proof of shipping upload on offer {}", offerId);
            var mav = new ModelAndView("account/proofOfShippingUpload");
            mav.addObject("offer", offerService.getById(offerId).orElseThrow(() -> NotFoundException.createFor("Offer")));
            mav.addObject("pendingOffersCount", getPendingOffersCount(currentUser));
            return mav;
        }

        offerService.uploadProofOfShipping(
                offerId,
                currentUser.getId(),
                form.getFileDto(),
                form.getTrackingNumber()
        );
        LOGGER.info("User {} uploaded proof of shipping for offer {}", currentUser.getId(), offerId);

        return new ModelAndView("redirect:/account/incoming-offers?statusGroup=pending_payment");
    }

    // Download proof of payment - buyer or seller
    @GetMapping("/my-offers/{offerId}/payment/download")
    public ResponseEntity<Resource> downloadProofOfPayment(@PathVariable Long offerId, @CurrentUser User currentUser) {
        LOGGER.debug("User {} downloading proof of payment for offer {}", currentUser.getId(), offerId);
        final Long currentUserId = currentUser.getId();
        final File file = offerService.getProofOfPaymentFile(offerId, currentUserId)
                .orElseThrow(() -> NotFoundException.createFor("Proof of payment not found"));

        final ByteArrayResource resource = new ByteArrayResource(file.getData());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType().orElse("application/octet-stream")))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .contentLength(file.getData().length)
                .body(resource);
    }

    // Download proof of shipping - buyer or seller
    @GetMapping("/my-offers/{offerId}/shipping/download")
    public ResponseEntity<Resource> downloadProofOfShipping(@PathVariable Long offerId, @CurrentUser User currentUser) {
        LOGGER.debug("User {} downloading proof of shipping for offer {}", currentUser.getId(), offerId);
        final Long currentUserId = currentUser.getId();
        final File file = offerService.getProofOfShippingFile(offerId, currentUserId)
                .orElseThrow(() -> NotFoundException.createFor("Proof of shipping not found"));

        final ByteArrayResource resource = new ByteArrayResource(file.getData());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType().orElse("application/octet-stream")))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .contentLength(file.getData().length)
                .body(resource);
    }

    // Rate offer page
    @GetMapping("/rate/{offerId}")
    public ModelAndView showRateForm(@PathVariable Long offerId, @CurrentUser User currentUser,
                                      @ModelAttribute("rateForm") RateForm rateForm) {
        final Offer offer = offerService.getById(offerId)
                .orElseThrow(() -> NotFoundException.createFor("Offer"));

        if (offer.getStatus() != OfferStatus.ACCEPTED) {
            throw new BadParameterException("Only accepted offers can be rated");
        }

        final Long currentUserId = currentUser.getId();
        final boolean isBuyer = offer.getBuyer().getId().equals(currentUserId);
        final boolean isSeller = offer.getListing().getCreator().getId().equals(currentUserId);

        if (!isBuyer && !isSeller) {
            throw new ForbiddenException("You did not participate in this offer");
        }

        // Check if already rated
        boolean alreadyRated = isBuyer ? offer.getSellerRating().isPresent() : offer.getBuyerRating().isPresent();
        if (alreadyRated) {
            throw new BadParameterException("You have already rated this offer");
        }

        var mav = new ModelAndView("account/rate");
        mav.addObject("offer", offer);
        mav.addObject("pendingOffersCount", getPendingOffersCount(currentUser));
        return mav;
    }

    @PostMapping("/rate/{offerId}")
    public ModelAndView submitRateForm(@PathVariable Long offerId,
                                        @CurrentUser User currentUser,
                                        @Valid @ModelAttribute("rateForm") RateForm rateForm,
                                        BindingResult bindingResult) {
        final Offer offer = offerService.getById(offerId)
                .orElseThrow(() -> NotFoundException.createFor("Offer")); // podria moverse a la primera linea de oferservice.rate

        if (bindingResult.hasErrors()) {
            var mav = new ModelAndView("account/rate");
            mav.addObject("offer", offer);
            mav.addObject("pendingOffersCount", getPendingOffersCount(currentUser));
            return mav;
        }

        offerService.rate(offer, currentUser, rateForm.getRating(), rateForm.getReviewText());

        final Long currentUserId = currentUser.getId();
        final boolean isBuyer = offer.getBuyer().getId().equals(currentUserId);

        String redirectUrl = isBuyer ? "/account/incoming-offers?statusGroup=resolved" : "/account/my-offers?statusGroup=resolved";
        return new ModelAndView("redirect:" + redirectUrl);
    }
}
