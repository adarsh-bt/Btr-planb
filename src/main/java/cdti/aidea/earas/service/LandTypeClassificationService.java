package cdti.aidea.earas.service;

import cdti.aidea.earas.model.Btr_models.Masters.LandTypeClassification;
import cdti.aidea.earas.repository.Btr_repo.LandTypeClassificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LandTypeClassificationService {

    @Autowired
    private LandTypeClassificationRepository landTypeClassificationRepository;

    public Map<String, String> getLandTypeClassificationMap() {
        List<LandTypeClassification> landTypeClassifications = landTypeClassificationRepository.findAll();

        Map<String, String> landTypeClassificationMap = new HashMap<>();
        for (LandTypeClassification classification : landTypeClassifications) {
            landTypeClassificationMap.put(classification.getLandType(), classification.getClassification());
        }

        return landTypeClassificationMap;
    }
}