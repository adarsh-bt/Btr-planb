package cdti.aidea.earas.service;

import cdti.aidea.earas.config.ExcelExportUtil;
import cdti.aidea.earas.contract.Response.BtrDataListResponse;
import cdti.aidea.earas.model.Btr_models.*;

import cdti.aidea.earas.model.Btr_models.Masters.TblLocalBody;
import cdti.aidea.earas.model.Btr_models.Masters.TblMasterVillage;
import cdti.aidea.earas.model.Btr_models.Masters.TblZoneRevenueVillageMapping;
import cdti.aidea.earas.repository.Btr_repo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class BtrExportService {


    private final UserZoneAssignmentRepositoty userZoneAssignmentRepositoty;
    private final TblZoneRevenueVillageMappingRepository tblZoneRevenueVillageMappingRepository;
    private final TblMasterVillageRepository tblMasterVillageRepository;
    private final TblBtrRepository tblBtrRepository;
    private final LocalBodyRepository localBodyRepository;
    private final LandTypeClassificationService landTypeClassificationService;
    private final TblBtrDataOldRepository tblBtrDataOldRepository;


    public ByteArrayResource exportUserBtrDataToExcel(UUID userId) {
        // Fetch user zone and related village
        var user = userZoneAssignmentRepositoty.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var zoneRevenueList = tblZoneRevenueVillageMappingRepository.findByZone(user.getTblMasterZone().getZoneId());

        List<Integer> villageIds = zoneRevenueList.stream()
                .map(TblZoneRevenueVillageMapping::getRevenueVillage)
                .toList();

        List<TblMasterVillage> villageList = tblMasterVillageRepository.findAllById(villageIds);
        List<Integer> lsgcodes = villageList.stream().map(TblMasterVillage::getLsgCode).toList();

        List<TblBtrDataOld> allData = tblBtrDataOldRepository.findAllByLsgcodeIn(lsgcodes);

        // Prepare maps
        Map<Integer, String> villageMap = villageList.stream()
                .collect(Collectors.toMap(TblMasterVillage::getLsgCode, TblMasterVillage::getVillageNameEn));

//        neww

        Map<String, String> localBodyNameMap = new HashMap<>();



//        new end
        List<String> lbCodes = allData.stream().map(TblBtrDataOld::getLbcode).distinct().toList();
        Map<Integer, String> localBodyMap = localBodyRepository.findAllByCodeApiIn(lbCodes).stream()
                .collect(Collectors.toMap(TblLocalBody::getLocalbodyId, TblLocalBody::getLocalbodyNameEn));

        Map<String, String> landTypeMap = landTypeClassificationService.getLandTypeClassificationMap();


        List<TblLocalBody> localBodies = localBodyRepository.findAllByCodeApiIn(lbCodes);
        localBodies.forEach(localBody ->
                localBodyNameMap.put(localBody.getCodeApi(), localBody.getLocalbodyNameEn())
        );

        // Transform into response DTO
        List<BtrDataListResponse> responseDtos = allData.stream()
                .map(data -> {
                    double totalCent = data.getArea() != null ? data.getArea() : 0;

                    BigDecimal bd = new BigDecimal(totalCent)
                            .setScale(2, RoundingMode.HALF_UP);

                    String formatted = bd.toPlainString(); // "10.00"

                    return new BtrDataListResponse(
                            data.getId(),
                            villageMap.get(data.getLsgcode()),
                            data.getBcode(),
                            data.getResvno(),
                            data.getResbdno(),
                            data.getLtype(),
                            localBodyNameMap.getOrDefault(data.getLbcode(), "N/A"),
                            data.getLtype(),
//                            BigDecimal.valueOf(totalCent).setScale(2, RoundingMode.HALF_UP).doubleValue()
                            formatted
                    );
                })
                .sorted(Comparator
                        .comparing(BtrDataListResponse::getLbname, Comparator.nullsLast(String::compareTo))
                        .thenComparing(BtrDataListResponse::getVillageName, Comparator.nullsLast(String::compareTo))
                        .thenComparing(BtrDataListResponse::getBcode)
                        .thenComparing(row -> {
                            try {
                                String[] parts = String.valueOf(row.getResvno()).split("/");
                                return Integer.parseInt(parts[0]) * 1000 + Integer.parseInt(parts.length > 1 ? parts[1] : "0");
                            } catch (Exception e) {
                                return Integer.MAX_VALUE;
                            }
                        })
                        .thenComparing(BtrDataListResponse::getLtype, Comparator.nullsLast(Comparator.reverseOrder()))
                )


                .collect(Collectors.toList());


        return ExcelExportUtil.generateExcel(responseDtos);
    }



//    public ByteArrayResource exportUserBtrDataToExcel(UUID userId) {
//        // Fetch user zone and related village
//        var user = userZoneAssignmentRepositoty.findByUserId(userId)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        var zoneRevenueList = tblZoneRevenueVillageMappingRepository.findByZone(user.getTblMasterZone().getZoneId());
//
//        List<Integer> villageIds = zoneRevenueList.stream()
//                .map(TblZoneRevenueVillageMapping::getRevenueVillage)
//                .toList();
//
//        List<TblMasterVillage> villageList = tblMasterVillageRepository.findAllById(villageIds);
//        List<Integer> lsgcodes = villageList.stream().map(TblMasterVillage::getLsgCode).toList();
//
//        List<TblBtrData> allData = tblBtrRepository.findAllByLsgcodeIn(lsgcodes);
//
//        // Prepare maps
//        Map<String, String> villageMap = villageList.stream()
//                .collect(Collectors.toMap(v -> String.valueOf(v.getVillageCodeApi()), TblMasterVillage::getVillageNameMal));
//
//        List<String> lbCodes = allData.stream().map(TblBtrData::getLbcode).distinct().toList();
//        Map<Integer, String> localBodyMap = localBodyRepository.findAllByCodeApiIn(lbCodes).stream()
//                .collect(Collectors.toMap(TblLocalBody::getLocalbodyId, TblLocalBody::getLocalbodyNameMal));
//
//        Map<String, String> landTypeMap = landTypeClassificationService.getLandTypeClassificationMap();
//
//        // Transform into response DTO
//        List<BtrDataListResponse> responseDtos = allData.stream()
//                .map(data -> {
//                    double totalCent = (data.getNhect() != null ? data.getNhect() : 0) * 247.13
//                            + (data.getNare() != null ? data.getNare() : 0) * 2.47
//                            + (data.getNsqm() != null ? data.getNsqm() : 0) * 0.02471;
//
//                    return new BtrDataListResponse(
//                            data.getId(),
//                            villageMap.getOrDefault(String.valueOf(data.getVcode()), "NA"),
//                            data.getBcode(),
//                            data.getResvno(),
//                            data.getResbdno(),
//                            data.getLbtype(),
//                            localBodyMap.getOrDefault(data.getLbcode(), "NA"),
//                            data.getLbcode(),
//                            data.getLtype(),
//                            BigDecimal.valueOf(totalCent).setScale(2, RoundingMode.HALF_UP).doubleValue()
//                    );
//                })
//                .collect(Collectors.toList());
//
//        return ExcelExportUtil.generateExcel(responseDtos);
//    }
}


