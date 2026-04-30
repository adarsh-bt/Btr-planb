//package cdti.aidea.earas.service;
//
//import cdti.aidea.earas.contract.Response.KeyplotCountResponse;
//import cdti.aidea.earas.model.Btr_models.KeyplotsLimitLog;
//import cdti.aidea.earas.repository.Btr_repo.KeyPlotsRepository;
//import cdti.aidea.earas.repository.Btr_repo.KeyplotsLimitLogRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class KeyplotCountService {
//    private final KeyplotsLimitLogRepository keyplotsLimitLogRepository;
//    private final KeyPlotsRepository keyPlotsRepository;
//
//
////public KeyplotCountResponse getKeyplotsUsageByZone(Integer zoneId) {
////
////    // 1️⃣ Get active keyplots limit
////    KeyplotsLimitLog limitLog =
////            keyplotsLimitLogRepository.findFirstByIsActiveTrueAndInActiveTrueOrderByCreatedAtDesc();
////
////    if (limitLog == null) {
////        throw new IllegalStateException("keyplots limit not configured");
////    }
////
//// Long allowedLimit = limitLog.getKeyplotsLimit();
////
////    // 2️⃣ Count used keyplots for zone
////   Long usedCount = keyPlotsRepository.countActiveKeyplotsByZone(zoneId);
////
////    // 3️⃣ Calculate remaining
////    Long remaining = allowedLimit - usedCount;
////
////    return new KeyplotCountResponse(
////            zoneId,
////            allowedLimit,
////            usedCount,
////            remaining
////    );
////}
//}