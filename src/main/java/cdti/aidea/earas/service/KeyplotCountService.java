package cdti.aidea.earas.service;

import cdti.aidea.earas.contract.Response.KeyplotCountResponse;
import cdti.aidea.earas.model.Btr_models.KeyplotsLimitLog;
import cdti.aidea.earas.repository.Btr_repo.KeyPlotsRepository;
import cdti.aidea.earas.repository.Btr_repo.KeyplotsLimitLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KeyplotCountService {
    private final KeyplotsLimitLogRepository keyplotsLimitLogRepository;
    private final KeyPlotsRepository keyPlotsRepository;

    public KeyplotCountResponse getKeyplotsLimitStatus(Integer zoneId) {

        // 1️⃣ Get keyplots limit where in_active = true
        KeyplotsLimitLog activeLimit =
                keyplotsLimitLogRepository.findFirstByIsActiveTrueAndIsInActiveTrueOrderByCreatedAtDesc();
        if (activeLimit != null) {
            System.out.println(">>> Keyplots Limit = " + activeLimit.getKeyplotsLimit());
            System.out.println("system "+keyplotsLimitLogRepository.findByIsInActiveTrueAndIsActiveFalse());
        }
System.out.println("  > Active limit =  "+activeLimit);
        if (activeLimit != null) {
            System.out.println(">>> Keyplots Limit = " + activeLimit.getKeyplotsLimit());
        }
        if (activeLimit == null) {
            throw new IllegalStateException(
                    "No active keyplots limit found (in_active = true)"
            );
        }

        Long allowedLimit = activeLimit.getKeyplotsLimit();

        // 2️⃣ Count only VALID keyplots
        Long usedCount =
                keyPlotsRepository.countActiveKeyplotsByZone(zoneId);
        usedCount = (usedCount == null) ? 0L : usedCount;

        // 3️⃣ Remaining count
        Long remaining = allowedLimit - usedCount;

        return new KeyplotCountResponse(
                zoneId,
                allowedLimit,
                usedCount,
                Math.max(remaining, 0)
        );
    }
//public KeyplotCountResponse getKeyplotsUsageByZone(Integer zoneId) {
//
//    // 1️⃣ Get active keyplots limit
//    KeyplotsLimitLog limitLog =
//            keyplotsLimitLogRepository.findFirstByIsActiveTrueAndInActiveTrueOrderByCreatedAtDesc();
//
//    if (limitLog == null) {
//        throw new IllegalStateException("keyplots limit not configured");
//    }
//
// Long allowedLimit = limitLog.getKeyplotsLimit();
//
//    // 2️⃣ Count used keyplots for zone
//   Long usedCount = keyPlotsRepository.countActiveKeyplotsByZone(zoneId);
//
//    // 3️⃣ Calculate remaining
//    Long remaining = allowedLimit - usedCount;
//
//    return new KeyplotCountResponse(
//            zoneId,
//            allowedLimit,
//            usedCount,
//            remaining
//    );
//}
}
