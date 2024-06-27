package jpabook.testtest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
class HospitalService {
    @Autowired
    private HospitalRepository hospitalRepository;

    @Transactional
    public void saveHospitals(List<Hospital> hospitals) {
        hospitalRepository.saveAll(hospitals);
    }

    public int getApiTotalCount() {
        // API에서 받은 총 개수를 반환하는 로직 구현
        // 예시로 하드코딩된 값을 반환합니다.
        return 76472; // 실제로는 API에서 받은 totalCount 값을 반환해야 합니다.
    }

    public int getDbTotalCount() {
        // DB에 저장된 총 개수를 반환하는 로직 구현
        return (int) hospitalRepository.count();
    }
}