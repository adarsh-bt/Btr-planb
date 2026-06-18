package cdti.aidea.earas.config;
import java.util.UUID;
import cdti.aidea.earas.contract.UserAccessDTOs.UserLoginDetailsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
@FeignClient(name = "userAccessClient", url = "http://localhost:8081/user-access")

public interface UserAccessClient {
    @GetMapping("/api/zone-access/{loginId}/user-details")
    UserLoginDetailsResponse getUserDetailsByLoginId(
            @PathVariable("loginId") UUID loginId);

}
