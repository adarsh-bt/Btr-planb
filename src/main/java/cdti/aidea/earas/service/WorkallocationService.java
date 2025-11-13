package cdti.aidea.earas.service;

import cdti.aidea.earas.contract.RequestsDTOs.TblWorkAllocationDTO;
import cdti.aidea.earas.model.Btr_models.TblWorkAllocation;
import cdti.aidea.earas.repository.Btr_repo.TblWorkAllocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkallocationService {

    private final TblWorkAllocationRepository tblWorkAllocationRepository;

    public List<TblWorkAllocationDTO> getWorkAllocationsByZoneId(Integer zoneId) {
        List<TblWorkAllocation> allocations = tblWorkAllocationRepository.findByZone_ZoneId(zoneId);

        return allocations.stream()
                .map(a -> new TblWorkAllocationDTO(
                        a.getId(),
                        a.getZone().getZoneId(),
                        a.getLbcode(),
                        a.getVillageWetArea(),
                        a.getVillageDryArea(),
                        a.getVillageTotalArea(),
                        a.getForestAreaA(),
                        a.getForestAreaB(),
                        a.getForestAreaC(),
                        a.getAreaUnderPlant(),
                        a.getForestExcludeUnclutivate(),
                        a.getForestExcludeNotUnclutivate(),
                        a.getKayalExcludeArea(),
                        a.getOtherExcludeFWet(),
                        a.getOtherExcludedFDry(),
                        a.getOtherExcludeFTotal(),
                        a.getNoOfPlotsWet(),
                        a.getNoOfPlotsDry(),
                        a.getNoOfPlotsTotal(),
                        a.getTotalAreaWet(),
                        a.getTotalAreaDry(),
                        a.getTotalAreaForEstimation(),
                        a.getRemarks(),
                        a.getUserId(),
                        a.getCreated(),
                        a.getUpdated(),
                        a.getIsActive(),
                        a.getAgriStart(),
                        a.getAgriEnd()
                ))
                .collect(Collectors.toList());
    }
}
