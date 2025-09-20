package cdti.aidea.earas.service;

import cdti.aidea.earas.contract.Response.BtrDataResponse;
import cdti.aidea.earas.model.Btr_models.TblBtrData;
import cdti.aidea.earas.model.Btr_models.Masters.TblLocalBody;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterVillage;
import cdti.aidea.earas.model.Btr_models.Masters.TblZoneRevenueVillageMapping;
import cdti.aidea.earas.repository.Btr_repo.TblBtrDataRepository;
import cdti.aidea.earas.repository.Btr_repo.LocalBodyRepository;
import cdti.aidea.earas.repository.Btr_repo.TblMasterVillageRepository;
import cdti.aidea.earas.repository.Btr_repo.TblZoneRevenueVillageMappingRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BtrFetchService {

    @Autowired
    private TblBtrDataRepository tblBtrDataRepository;

    @Autowired
    private TblMasterVillageRepository tblMasterVillageRepository;

    @Autowired
    private LocalBodyRepository localBodyRepository;

    @Autowired
    private TblZoneRevenueVillageMappingRepository zoneRevenueVillageMappingRepository;

    /**
     * Fetch all BTR data for a specific zone with optional filtering
     * @param zoneId The zone ID to fetch data for
     * @param filter Optional search filter for block, survey number, or land type
     * @return List of BTR data responses
     */
    public List<BtrDataResponse> getAllBtrDataByZone(Integer zoneId, String filter) {
        try {
            // Get village IDs mapped to the zone
            List<Integer> villageIds = getVillageIdsByZone(zoneId);

            if (villageIds.isEmpty()) {
                System.out.println("No villages found for zone: " + zoneId);
                return new ArrayList<>();
            }

            // Get village details
            List<TblMasterVillage> villageList = tblMasterVillageRepository.findAllById(villageIds);
            List<Integer> lsgcodes = villageList.stream()
                    .map(TblMasterVillage::getLsgCode)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            System.out.println("Zone ID: " + zoneId + ", Villages: " + villageIds.size() + ", LSG Codes: " + lsgcodes.size());

            if (lsgcodes.isEmpty()) {
                System.out.println("No valid LSG codes found for zone: " + zoneId);
                return new ArrayList<>();
            }

            // Fetch BTR data based on LSG codes
            List<TblBtrData> allData = tblBtrDataRepository.findAllByLsgcodeIn(lsgcodes);

            // Apply filter if provided
            if (filter != null && !filter.trim().isEmpty()) {
                final String finalFilter = filter; // Make effectively final for lambda
                allData = allData.stream()
                        .filter(data -> matchesFilter(data, finalFilter))
                        .collect(Collectors.toList());
            }

            System.out.println("Found " + allData.size() + " BTR records for zone " + zoneId +
                    (filter != null ? " with filter: " + filter : ""));

            // Convert to response DTOs with additional information
            return convertToResponseDTOs(allData, villageList, zoneId);

        } catch (Exception e) {
            System.err.println("Error fetching BTR data for zone " + zoneId + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Get BTR data by specific ID with all related information
     * @param id The BTR record ID
     * @return BtrDataResponse with complete details
     */
    public Optional<BtrDataResponse> getBtrDataById(Long id) {
        try {
            if (id == null || id <= 0) {
                return Optional.empty();
            }

            Optional<TblBtrData> btrDataOpt = tblBtrDataRepository.findById(id);

            if (btrDataOpt.isEmpty()) {
                System.out.println("No BTR record found with ID: " + id);
                return Optional.empty();
            }

            TblBtrData btrData = btrDataOpt.get();

            // Get village information
            TblMasterVillage village = null;
            if (btrData.getLsgcode() != null) {
                Optional<TblMasterVillage> villageOpt = tblMasterVillageRepository.findByLsgCode(btrData.getLsgcode());
                village = villageOpt.orElse(null);
            }

            // Get local body information
            TblLocalBody localBody = null;
            if (btrData.getLbcode() != null) {
                Optional<TblLocalBody> localBodyOpt = localBodyRepository.findByCodeApi(btrData.getLbcode());
                localBody = localBodyOpt.orElse(null);
            }

            // Get zone information
            Integer zoneId = findZoneByVillageId(village != null ? village.getVillageId() : null);

            // Convert to response DTO
            BtrDataResponse response = convertToResponseDTO(btrData);

            // Add related information
            if (village != null) {
                response.setVillageName(village.getVillageNameEn());
            }
            if (localBody != null) {
                response.setLocalBodyName(localBody.getLocalbodyNameEn());
            }
            response.setZoneId(zoneId);

            return Optional.of(response);

        } catch (Exception e) {
            System.err.println("Error fetching BTR data by ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Get BTR records by multiple IDs
     * @param ids List of BTR record IDs
     * @return List of BtrDataResponse objects
     */
    public List<BtrDataResponse> getBtrDataByIds(List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return new ArrayList<>();
            }

            // Filter out null and invalid IDs
            List<Long> validIds = ids.stream()
                    .filter(Objects::nonNull)
                    .filter(id -> id > 0)
                    .distinct()
                    .collect(Collectors.toList());

            if (validIds.isEmpty()) {
                return new ArrayList<>();
            }

            List<TblBtrData> btrDataList = tblBtrDataRepository.findAllById(validIds);

            if (btrDataList.isEmpty()) {
                System.out.println("No BTR records found for provided IDs");
                return new ArrayList<>();
            }

            // Get unique LSG codes for village lookup
            Set<Integer> lsgCodes = btrDataList.stream()
                    .map(TblBtrData::getLsgcode)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            // Get villages - FIXED: make it effectively final
            final List<TblMasterVillage> villages;
            if (!lsgCodes.isEmpty()) {
                villages = tblMasterVillageRepository.findByLsgCodeIn(lsgCodes);
            } else {
                villages = new ArrayList<>();
            }

            // Get unique local body codes
            List<String> lbCodes = btrDataList.stream()
                    .map(TblBtrData::getLbcode)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            // Get local bodies - FIXED: make it effectively final
            final List<TblLocalBody> localBodies;
            if (!lbCodes.isEmpty()) {
                localBodies = localBodyRepository.findAllByCodeApiIn(lbCodes);
            } else {
                localBodies = new ArrayList<>();
            }

            // Create lookup maps
            Map<Integer, String> villageNameMap = villages.stream()
                    .collect(Collectors.toMap(
                            TblMasterVillage::getLsgCode,
                            TblMasterVillage::getVillageNameEn,
                            (existing, replacement) -> existing
                    ));

            Map<String, String> localBodyNameMap = localBodies.stream()
                    .collect(Collectors.toMap(
                            TblLocalBody::getCodeApi,
                            TblLocalBody::getLocalbodyNameEn,
                            (existing, replacement) -> existing
                    ));

            // Create village to zone mapping
            final Map<Integer, Integer> villageToZoneMap = createVillageToZoneMap(villages);

            // Convert to response DTOs - FIXED: all variables are now effectively final
            return btrDataList.stream()
                    .map(data -> {
                        BtrDataResponse response = convertToResponseDTO(data);
                        response.setVillageName(villageNameMap.get(data.getLsgcode()));
                        response.setLocalBodyName(localBodyNameMap.get(data.getLbcode()));

                        // Find zone for this record
                        Optional<TblMasterVillage> village = villages.stream()
                                .filter(v -> v.getLsgCode().equals(data.getLsgcode()))
                                .findFirst();

                        Integer zoneId = village.map(v -> villageToZoneMap.get(v.getVillageId())).orElse(null);
                        response.setZoneId(zoneId);

                        return response;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error fetching BTR data by IDs: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Get summary statistics for a zone
     * @param zoneId The zone ID
     * @return Summary string with zone statistics
     */
    public String getZoneSummary(Integer zoneId) {
        try {
            List<BtrDataResponse> data = getAllBtrDataByZone(zoneId, null);

            long totalRecords = data.size();
            long uniqueLocalBodies = data.stream()
                    .map(BtrDataResponse::getLbcode)
                    .filter(Objects::nonNull)
                    .distinct()
                    .count();
            long uniqueVillages = data.stream()
                    .map(BtrDataResponse::getLsgcode)
                    .filter(Objects::nonNull)
                    .distinct()
                    .count();

            return String.format("Zone %d: %d BTR records across %d local bodies and %d villages",
                    zoneId, totalRecords, uniqueLocalBodies, uniqueVillages);

        } catch (Exception e) {
            System.err.println("Error getting zone summary for zone " + zoneId + ": " + e.getMessage());
            return String.format("Zone %d: Error retrieving summary", zoneId);
        }
    }

    /**
     * Get unique land types for a zone
     * @param zoneId The zone ID
     * @return Map of land types with counts
     */
    public Map<String, Long> getLandTypesByZone(Integer zoneId) {
        try {
            List<BtrDataResponse> data = getAllBtrDataByZone(zoneId, null);

            return data.stream()
                    .collect(Collectors.groupingBy(
                            response -> response.getLtype() != null && !response.getLtype().trim().isEmpty()
                                    ? response.getLtype() : "Unknown",
                            Collectors.counting()
                    ));

        } catch (Exception e) {
            System.err.println("Error getting land types for zone " + zoneId + ": " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Get unique local bodies for a zone
     * @param zoneId The zone ID
     * @return Map of local body code to name
     */
    public Map<String, String> getLocalBodiesByZone(Integer zoneId) {
        try {
            List<BtrDataResponse> data = getAllBtrDataByZone(zoneId, null);

            return data.stream()
                    .filter(response -> response.getLbcode() != null && response.getLocalBodyName() != null)
                    .collect(Collectors.toMap(
                            BtrDataResponse::getLbcode,
                            BtrDataResponse::getLocalBodyName,
                            (existing, replacement) -> existing
                    ));

        } catch (Exception e) {
            System.err.println("Error getting local bodies for zone " + zoneId + ": " + e.getMessage());
            return new HashMap<>();
        }
    }

    // Private helper methods

    /**
     * Get village IDs mapped to a specific zone
     */
    private List<Integer> getVillageIdsByZone(Integer zoneId) {
        if (zoneId == null) {
            return new ArrayList<>();
        }

        try {
            List<TblZoneRevenueVillageMapping> zoneRevenueList =
                    zoneRevenueVillageMappingRepository.findByZone(zoneId);

            return zoneRevenueList.stream()
                    .filter(mapping -> mapping.getIsValid() != null && mapping.getIsValid())
                    .map(TblZoneRevenueVillageMapping::getRevenueVillage)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Error getting villages for zone " + zoneId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Check if BTR data matches the search filter
     */
    private boolean matchesFilter(TblBtrData data, String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            return true;
        }

        String filterLower = filter.toLowerCase().trim();

        return (data.getBcode() != null && data.getBcode().toLowerCase().contains(filterLower)) ||
                (data.getResvno() != null && data.getResvno().toString().contains(filterLower)) ||
                (data.getResbdno() != null && data.getResbdno().toLowerCase().contains(filterLower)) ||
                (data.getLtype() != null && data.getLtype().toLowerCase().contains(filterLower)) ||
                (data.getLanduse() != null && data.getLanduse().toLowerCase().contains(filterLower));
    }

    /**
     * Convert BTR entity data to response DTOs with additional information
     */
    private List<BtrDataResponse> convertToResponseDTOs(List<TblBtrData> btrDataList,
                                                        List<TblMasterVillage> villageList, Integer zoneId) {

        if (btrDataList.isEmpty()) {
            return new ArrayList<>();
        }

        // Create village lookup map
        Map<Integer, String> villageNameMap = villageList.stream()
                .collect(Collectors.toMap(
                        TblMasterVillage::getLsgCode,
                        TblMasterVillage::getVillageNameEn,
                        (existing, replacement) -> existing
                ));

        // Get local body names for all unique lbcodes
        List<String> lbcodes = btrDataList.stream()
                .map(TblBtrData::getLbcode)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // Make localBodyNameMap effectively final
        final Map<String, String> localBodyNameMap;
        Map<String, String> localBodyNameMap1;
        if (!lbcodes.isEmpty()) {
            try {
                localBodyNameMap1 = localBodyRepository.findAllByCodeApiIn(lbcodes)
                        .stream()
                        .collect(Collectors.toMap(
                                TblLocalBody::getCodeApi,
                                TblLocalBody::getLocalbodyNameEn,
                                (existing, replacement) -> existing
                        ));
            } catch (Exception e) {
                System.err.println("Error fetching local body names: " + e.getMessage());
                localBodyNameMap1 = new HashMap<>();
            }
        } else {
            localBodyNameMap1 = new HashMap<>();
        }

        localBodyNameMap = localBodyNameMap1;
        return btrDataList.stream()
                .map(data -> {
                    BtrDataResponse response = convertToResponseDTO(data);
                    response.setVillageName(villageNameMap.get(data.getLsgcode()));
                    response.setLocalBodyName(localBodyNameMap.get(data.getLbcode()));
                    response.setZoneId(zoneId);
                    return response;
                })
                .sorted(Comparator.comparing((BtrDataResponse r) -> r.getLocalBodyName() != null ? r.getLocalBodyName() : "")
                        .thenComparing(r -> r.getVillageName() != null ? r.getVillageName() : "")
                        .thenComparing(r -> r.getBcode() != null ? r.getBcode() : "")
                        .thenComparing(r -> r.getResvno() != null ? r.getResvno() : 0)
                        .thenComparing(r -> r.getResbdno() != null ? r.getResbdno() : ""))
                .collect(Collectors.toList());
    }

    /**
     * Convert single BTR entity to response DTO
     */
    private BtrDataResponse convertToResponseDTO(TblBtrData data) {
        BtrDataResponse response = new BtrDataResponse();
        response.setId(data.getId());
        response.setDcode(data.getDcode());
        response.setTcode(data.getTcode());
        response.setVcode(data.getVcode());
        response.setBcode(data.getBcode());
        response.setResvno(data.getResvno());
        response.setResbdno(data.getResbdno());
        response.setLbtype(data.getLbtype());
        response.setLbcode(data.getLbcode());
        response.setGovpriv(data.getGovpriv());
        response.setLtype(data.getLtype());
        response.setLanduse(data.getLanduse());
        response.setNhect(data.getNhect());
        response.setNare(data.getNare());
        response.setNsqm(data.getNsqm());
        response.setEast(data.getEast());
        response.setWest(data.getWest());
        response.setNorth(data.getNorth());
        response.setSouth(data.getSouth());
        response.setLsgcode(data.getLsgcode());
        response.setTotCent(data.getTotCent());

        // Create combined survey number
        String surveyNumber = data.getResvno() != null ? data.getResvno().toString() : "";
        if (data.getResbdno() != null && !data.getResbdno().trim().isEmpty()) {
            surveyNumber += "/" + data.getResbdno();
        }
        response.setSurveyNumber(surveyNumber.isEmpty() ? null : surveyNumber);

        return response;
    }

    /**
     * Helper method to find zone ID by village ID
     */
    private Integer findZoneByVillageId(Integer villageId) {
        if (villageId == null) {
            return null;
        }

        try {
            List<TblZoneRevenueVillageMapping> mappings = zoneRevenueVillageMappingRepository.findAll()
                    .stream()
                    .filter(mapping -> mapping.getRevenueVillage() != null &&
                            mapping.getRevenueVillage().equals(villageId) &&
                            mapping.getIsValid() != null && mapping.getIsValid())
                    .collect(Collectors.toList());

            return mappings.isEmpty() ? null : mappings.get(0).getZone();
        } catch (Exception e) {
            System.err.println("Error finding zone for village " + villageId + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Create a map of village ID to zone ID for efficient lookup
     */
    private Map<Integer, Integer> createVillageToZoneMap(List<TblMasterVillage> villages) {
        Map<Integer, Integer> villageToZoneMap = new HashMap<>();

        for (TblMasterVillage village : villages) {
            Integer zoneId = findZoneByVillageId(village.getVillageId());
            if (zoneId != null) {
                villageToZoneMap.put(village.getVillageId(), zoneId);
            }
        }

        return villageToZoneMap;
    }
}
