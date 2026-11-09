package ar.edu.itba.paw.webapp.form.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = BrandModelRequestAcceptValidator.class)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBrandModelRequestAccept {
    String message() default "{ValidBrandModelRequestAccept.adminBrandModelRequestAcceptForm}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
