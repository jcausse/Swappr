package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Province;
import java.util.List;
import java.util.Optional;

public interface ProvinceDao {
    Optional<Province> getById(Long id);
    List<Province> getAll();
}
