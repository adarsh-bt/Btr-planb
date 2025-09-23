package cdti.aidea.earas.service;

import cdti.aidea.earas.contract.LocalbodyDto;
import cdti.aidea.earas.contract.RevenueTalukDto;
import cdti.aidea.earas.contract.RevenueVillageDto;
import cdti.aidea.earas.model.Btr_models.Masters.*;
import cdti.aidea.earas.repository.Btr_repo.*;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ZoneMappingService {

  private final TblZoneLocalbodyMappingRepository zoneLbRepo;
  private final LocalBodyRepository localBodyRepo;
  private final ZoneRevenueTalukMappingRepository zoneRevenueTalukMappingRepository;
  private final TblZoneRevenueVillageMappingRepository zoneRevenueVillageMappingRepository;
  private final TblMasterVillageRepository tblMasterVillageRepository;
  private final TblMasterVillageBlockRepository tblMasterVillageBlockRepository;
  private final TblMasterZoneRepository tblMasterZoneRepository;

  // New Method: Get District ID by Zone ID
  public Optional<Integer> getDistrictIdByZone(Integer zoneId) {
    if (zoneId == null) {
      throw new IllegalArgumentException("zoneId must not be null");
    }
    // Use the findById method from JpaRepository
    return tblMasterZoneRepository.findById(zoneId).map(TblMasterZone::getDistId);
  }

  // Localbodies by Zone
  public List<LocalbodyDto> getLocalbodiesByZone(Integer zoneId, String lang) {
    List<TblZoneLocalbodyMapping> mappings =
        zoneLbRepo.findAllByZoneAndIsValid(zoneId, Boolean.TRUE);

    if (mappings.isEmpty()) return List.of();

    List<Integer> lbIds =
        mappings.stream()
            .map(TblZoneLocalbodyMapping::getLocalbody)
            .filter(Objects::nonNull)
            .distinct()
            .toList();

    List<TblLocalBody> localbodies = localBodyRepo.findAllById(lbIds);

    boolean mal = "mal".equalsIgnoreCase(lang) || "ml".equalsIgnoreCase(lang);

    return localbodies.stream()
        .filter(lb -> Boolean.TRUE.equals(lb.getIsActive()))
        .map(
            lb ->
                new LocalbodyDto(
                    lb.getLocalbodyId(),
                    mal ? lb.getLocalbodyNameMal() : lb.getLocalbodyNameEn(),
                    lb.getCodeApi()))
        .toList();
  }

  // Revenue Taluks by Zone
  public List<RevenueTalukDto> getRevenueTaluksByZone(Integer zoneId, String lang) {
    if (zoneId == null) throw new IllegalArgumentException("zoneId must not be null");

    List<ZoneRevenueTalukMapping> mappings = zoneRevenueTalukMappingRepository.findByZone(zoneId);

    return mappings.stream()
        .filter(m -> Boolean.TRUE.equals(m.getIsValid()))
        .filter(m -> Objects.nonNull(m.getRevenueTaluk()))
        .map(
            m ->
                new RevenueTalukDto(
                    m.getRevenueTaluk(), m.getRevTalukNameEn() // only English
                    ))
        .distinct()
        .sorted(
            Comparator.comparing(
                RevenueTalukDto::getRevenueTalukName, String.CASE_INSENSITIVE_ORDER))
        .toList();
  }

  // Optimized: Revenue Villages by Zone with blockCode
  public List<RevenueVillageDto> getRevenueVillagesByZone(Integer zoneId, String lang) {
    if (zoneId == null) throw new IllegalArgumentException("zoneId must not be null");

    boolean mal = "mal".equalsIgnoreCase(lang) || "ml".equalsIgnoreCase(lang);

    List<TblZoneRevenueVillageMapping> mappings =
        zoneRevenueVillageMappingRepository.findByZone(zoneId);

    List<Integer> villageIds =
        mappings.stream()
            .filter(m -> Boolean.TRUE.equals(m.getIsValid()))
            .map(TblZoneRevenueVillageMapping::getRevenueVillage)
            .filter(Objects::nonNull)
            .distinct()
            .toList();

    if (villageIds.isEmpty()) return List.of();

    List<TblMasterVillage> villages = tblMasterVillageRepository.findAllById(villageIds);
    List<TblMasterVillageBlock> blocks =
        tblMasterVillageBlockRepository.findByVillageIdIn(villageIds);

    // Map villageId -> list of blockCodes
    Map<Integer, List<String>> villageBlockMap =
        blocks.stream()
            .collect(
                Collectors.groupingBy(
                    TblMasterVillageBlock::getVillageId,
                    Collectors.mapping(TblMasterVillageBlock::getBlockCode, Collectors.toList())));

    return villages.stream()
        .map(
            v ->
                new RevenueVillageDto(
                    v.getVillageId(),
                    mal ? v.getVillageNameMal() : v.getVillageNameEn(),
                    villageBlockMap.getOrDefault(v.getVillageId(), List.of()),
                    v.getLsgCode()))
        .distinct()
        .sorted(
            Comparator.comparing(
                RevenueVillageDto::getRevenueVillageName, String.CASE_INSENSITIVE_ORDER))
        .toList();
  }
}
