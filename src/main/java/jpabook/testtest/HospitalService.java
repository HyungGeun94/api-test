package jpabook.testtest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HospitalService {

    @Autowired
    private HospitalRepository repository;

    @Transactional
    public void saveHospitals(List<Hospital> hospitals) {
        repository.saveAll(hospitals);
    }
}
