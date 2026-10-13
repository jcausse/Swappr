package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Language;
import ar.edu.itba.paw.model.Listing;
import ar.edu.itba.paw.model.Offer;
import ar.edu.itba.paw.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class MailingServiceImpl implements MailingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailingServiceImpl.class);

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine emailTemplateEngine;
    private final MessageSource messageSource;
    private final String mailFrom;
    private final String baseUrl;

    public MailingServiceImpl(
            JavaMailSender mailSender,
            @Qualifier("emailTemplateEngine") SpringTemplateEngine emailTemplateEngine,
            MessageSource messageSource,
            @Value("${mail.username}") String mailFrom,
            @Value("${app.baseUrl}") String baseUrl
    ) {
        this.mailSender = mailSender;
        this.emailTemplateEngine = emailTemplateEngine;
        this.messageSource = messageSource;
        this.mailFrom = mailFrom;
        this.baseUrl = baseUrl;
    }

    @Async
    @Override
    public void sendWelcomeEmail(User user) {
        var context = new Context(localeOf(user));
        context.setVariable("user", user);
        context.setVariable("baseUrl", baseUrl);
        context.setVariable("actionUrl", baseUrl + "/profile/" + user.getId());

        String subject = messageSource.getMessage("email.welcome.subject", null, localeOf(user));
        sendEmail(user.getEmail(), subject, "welcome", context);
    }

    @Async
    @Override
    public void sendListingPublishedEmail(User seller, Listing listing) {
        var context = new Context(localeOf(seller));
        context.setVariable("user", seller);
        context.setVariable("listing", listing);
        context.setVariable("baseUrl", baseUrl);
        context.setVariable("actionUrl", baseUrl + "/listing/" + listing.getId());

        String subject = messageSource.getMessage("email.listing.subject", null, localeOf(seller));
        sendEmail(seller.getEmail(), subject, "listing-published", context);
    }

    @Async
    @Override
    public void sendPurchaseSellerEmail(User seller, User buyer, Listing listing, String message) {
        var context = new Context(localeOf(seller));
        context.setVariable("seller", seller);
        context.setVariable("buyer", buyer);
        context.setVariable("listing", listing);
        context.setVariable("message", message);
        context.setVariable("baseUrl", baseUrl);
        context.setVariable("actionUrl", "mailto:" + buyer.getEmail());

        String subject = messageSource.getMessage("email.purchase.seller.subject", null, localeOf(seller));
        sendEmail(seller.getEmail(), subject, "purchase-seller", context);
    }

    @Async
    @Override
    public void sendPurchaseBuyerEmail(User buyer, User seller, Listing listing) {
        var context = new Context(localeOf(buyer));
        context.setVariable("buyer", buyer);
        context.setVariable("seller", seller);
        context.setVariable("listing", listing);
        context.setVariable("baseUrl", baseUrl);
        context.setVariable("actionUrl", "mailto:" + seller.getEmail());

        String subject = messageSource.getMessage("email.purchase.buyer.subject", null, localeOf(buyer));
        sendEmail(buyer.getEmail(), subject, "purchase-buyer", context);
    }

    @Async
    @Override
    public void sendNewOfferEmail(User seller, User buyer, Listing listing, Offer offer) {
        var context = new Context(localeOf(seller));
        context.setVariable("buyer", buyer);
        context.setVariable("listing", listing);
        context.setVariable("offer", offer);
        context.setVariable("baseUrl", baseUrl);
        context.setVariable("actionUrl", baseUrl + "/account/incoming-offers/" + offer.getId());

        String subject = messageSource.getMessage("email.offer.new.subject", null, localeOf(seller));
        sendEmail(seller.getEmail(), subject, "offer-new", context);
    }

    @Async
    @Override
    public void sendOfferRejectedEmail(User buyer, Listing listing, Offer offer) {
        var context = new Context(localeOf(buyer));
        context.setVariable("buyer", buyer);
        context.setVariable("listing", listing);
        context.setVariable("offer", offer);
        context.setVariable("baseUrl", baseUrl);

        String subject = messageSource.getMessage("email.offer.rejected.subject", null, localeOf(buyer));
        sendEmail(buyer.getEmail(), subject, "offer-rejected", context);
    }

    @Async
    @Override
    public void sendOfferWithdrawnEmail(User seller, User buyer, Listing listing, Offer offer) {
        var context = new Context(localeOf(seller));
        context.setVariable("seller", seller);
        context.setVariable("buyer", buyer);
        context.setVariable("listing", listing);
        context.setVariable("offer", offer);
        context.setVariable("baseUrl", baseUrl);
        context.setVariable("actionUrl", baseUrl + "/listing/" + listing.getId());

        String subject = messageSource.getMessage("email.offer.withdrawn.subject", null, localeOf(seller));
        sendEmail(seller.getEmail(), subject, "offer-withdrawn", context);
    }

    @Async
    @Override
    public void sendOfferPendingPaymentEmail(User buyer, User seller, Listing listing, Offer offer) {
        var context = new Context(localeOf(buyer));
        context.setVariable("buyer", buyer);
        context.setVariable("seller", seller);
        context.setVariable("listing", listing);
        context.setVariable("offer", offer);
        context.setVariable("baseUrl", baseUrl);
        context.setVariable("actionUrl", baseUrl + "/account/my-offers/" + offer.getId() + "/payment");

        String subject = messageSource.getMessage("email.offer.pendingPayment.subject", null, localeOf(buyer));
        sendEmail(buyer.getEmail(), subject, "offer-pending-payment", context);
    }

    @Async
    @Override
    public void sendPendingTransactionEmail(User seller, User buyer, Listing listing, Offer offer) {
        var context = new Context(localeOf(seller));
        context.setVariable("seller", seller);
        context.setVariable("buyer", buyer);
        context.setVariable("listing", listing);
        context.setVariable("offer", offer);
        context.setVariable("baseUrl", baseUrl);
        context.setVariable("actionUrl", baseUrl + "/account/incoming-offers/" + offer.getId());

        String subject = messageSource.getMessage("email.pendingTransaction.subject", null, localeOf(seller));
        sendEmail(seller.getEmail(), subject, "pending-transaction", context);
    }

    @Async
    @Override
    public void sendProofOfPaymentUploadedEmail(User seller, User buyer, Listing listing, Offer offer) {
        var context = new Context(localeOf(seller));
        context.setVariable("seller", seller);
        context.setVariable("buyer", buyer);
        context.setVariable("listing", listing);
        context.setVariable("offer", offer);
        context.setVariable("baseUrl", baseUrl);
        context.setVariable("actionUrl", baseUrl + "/account/incoming-offers/" + offer.getId());

        String subject = messageSource.getMessage("email.proofOfPayment.uploaded.subject", null, localeOf(seller));
        sendEmail(seller.getEmail(), subject, "proof-of-payment-uploaded", context);
    }

    @Async
    @Override
    public void sendProofOfShippingUploadedEmail(User buyer, User seller, Listing listing, Offer offer) {
        var context = new Context(localeOf(buyer));
        context.setVariable("buyer", buyer);
        context.setVariable("seller", seller);
        context.setVariable("listing", listing);
        context.setVariable("offer", offer);
        context.setVariable("baseUrl", baseUrl);
        context.setVariable("actionUrl", baseUrl + "/account/my-offers");
        context.setVariable("trackingNumber", offer.getTrackingNumber());

        String subject = messageSource.getMessage("email.proofOfShipping.uploaded.subject", null, localeOf(buyer));
        sendEmail(buyer.getEmail(), subject, "proof-of-shipping-uploaded", context);
    }

    @Async
    @Override
    public void sendPasswordRecoveryEmail(User user, String otpValue) {
        var context = new Context(localeOf(user));
        context.setVariable("user", user);
        context.setVariable("otpValue", otpValue);
        context.setVariable("baseUrl", baseUrl);
        context.setVariable("actionUrl", baseUrl + "/recovery/verification?email="
                + URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8)
                + "&otp=" + URLEncoder.encode(otpValue, StandardCharsets.UTF_8));

        String subject = messageSource.getMessage("email.passwordRecovery.subject", null, localeOf(user));
        sendEmail(user.getEmail(), subject, "password-recovery", context);
    }

    @Async
    @Override
    public void sendVerificationEmail(User user, String otpValue) {
        var context = new Context(localeOf(user));
        context.setVariable("user", user);
        context.setVariable("otpValue", otpValue);
        context.setVariable("baseUrl", baseUrl);
        context.setVariable("actionUrl", baseUrl + "/verify?email="
                + URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8)
                + "&otp=" + URLEncoder.encode(otpValue, StandardCharsets.UTF_8));

        String subject = messageSource.getMessage("email.verification.subject", null, localeOf(user));
        sendEmail(user.getEmail(), subject, "email-verification", context);
    }

    /**
     * The locale to use for an email, derived from the recipient's preferred language
     * (not the acting user's request locale). This is what makes emails arrive in the
     * recipient's language regardless of who triggered the action.
     */
    private static Locale localeOf(final User recipient) {
        final Language language = recipient.getPreferredLanguage() != null
                ? recipient.getPreferredLanguage()
                : Language.getDefault();
        return new Locale(language.getCode());
    }

    @Async
    @Override
    public void sendAccountSuspendedEmail(User user) {
        var context = new Context(localeOf(user));
        context.setVariable("user", user);
        context.setVariable("baseUrl", baseUrl);
        context.setVariable("actionUrl", baseUrl);

        String subject = messageSource.getMessage("email.user.suspended.subject", null, localeOf(user));
        sendEmail(user.getEmail(), subject, "user-suspended", context);
    }

    @Async
    @Override
    public void sendAccountUnsuspendedEmail(User user) {
        var context = new Context(localeOf(user));
        context.setVariable("user", user);
        context.setVariable("baseUrl", baseUrl);
        context.setVariable("actionUrl", baseUrl + "/login");

        String subject = messageSource.getMessage("email.user.unsuspended.subject", null, localeOf(user));
        sendEmail(user.getEmail(), subject, "user-unsuspended", context);
    }


    private void sendEmail(String to, String subject, String templateName, Context context) {
        final var maskedEmail = maskEmail(to);
        LOGGER.info("Sending '{}' email to {}", templateName, maskedEmail);
        try {
            var mimeMessage = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject(subject);

            var htmlContent = emailTemplateEngine.process(templateName, context);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            LOGGER.debug("Email '{}' sent successfully to {}", templateName, maskedEmail);
        } catch (Exception e) {
            LOGGER.error("Failed to send '{}' email to {}: {}", templateName, maskedEmail, e.getMessage(), e);
        }
    }

    private static String maskEmail(String email) {
        if (email == null) return "null";
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) return "***";
        String prefix = email.substring(0, Math.min(3, atIndex));
        String domain = email.substring(atIndex);
        return prefix + "***" + domain;
    }
}
