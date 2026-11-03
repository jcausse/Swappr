package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Province;
import java.util.List;
import java.util.Optional;

public interface ProvinceService {
    List<Province> getAll();
    Optional<Province> getById(Long id);
}
