package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.model.BrandModelRequestType;

// Fields shared by the request form and the admin accept form, checked by BrandModelRequestBrandRule.
public interface BrandModelRequestFields {

    BrandModelRequestType getType();

    Long getBrandId();

    String getNewBrandName();
}
