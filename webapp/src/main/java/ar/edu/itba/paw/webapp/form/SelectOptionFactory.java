package ar.edu.itba.paw.webapp.form;

import ar.edu.itba.paw.model.Category;
import ar.edu.itba.paw.model.OfferStatusGroup;
import ar.edu.itba.paw.model.Province;
import ar.edu.itba.paw.model.Subcategory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public final class SelectOptionFactory {

    private SelectOptionFactory() {
    }

    public static List<SelectOption> categories(final Collection<Category> categories) {
        return categories.stream()
                .map(category -> new SelectOption(category.getId(), "category." + category.getName()))
                .toList();
    }

    public static List<SelectOption> subcategories(final Collection<Subcategory> subcategories) {
        return subcategories.stream()
                .map(subcategory -> new SelectOption(subcategory.getId(), "subcategory." + subcategory.getName()))
                .toList();
    }

    public static List<SelectOption> provinces(final Collection<Province> provinces) {
        return provinces.stream()
                .map(province -> new SelectOption(province.getId(), "province." + province.getName()))
                .toList();
    }

    public static List<StringSelectOption> offerStatusGroups() {
        return Arrays.stream(OfferStatusGroup.values())
                .map(statusGroup -> new StringSelectOption(
                        statusGroup.name(), "offer.statusGroup." + statusGroup.getStatus()))
                .toList();
    }
}
