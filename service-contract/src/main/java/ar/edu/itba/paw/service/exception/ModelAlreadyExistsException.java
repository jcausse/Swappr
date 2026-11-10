package ar.edu.itba.paw.service.exception;

import ar.edu.itba.paw.model.Model;

public class ModelAlreadyExistsException extends RuntimeException {

    private final Long modelId;
    private final String brandName;
    private final String modelName;

    public ModelAlreadyExistsException(Long modelId, String brandName, String modelName) {
        super("Model '" + modelName + "' already exists for brand '" + brandName + "'");
        this.modelId = modelId;
        this.brandName = brandName;
        this.modelName = modelName;
    }

    public static ModelAlreadyExistsException createFor(Model model) {
        return new ModelAlreadyExistsException(
            model.getId(),
            model.getBrand().getName(),
            model.getName()
        );
    }

    public Long getModelId() {
        return modelId;
    }

    public String getBrandName() {
        return brandName;
    }

    public String getModelName() {
        return modelName;
    }
}
