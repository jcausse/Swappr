package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Province;
import ar.edu.itba.paw.persistence.ProvinceDao;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ProvinceServiceImpl implements ProvinceService {

    private final ProvinceDao provinceDao;

    @Override
    public List<Province> getAll() {
        return provinceDao.getAll();
    }

    @Override
    public Optional<Province> getById(Long id) {
        return provinceDao.getById(id);
    }
}
