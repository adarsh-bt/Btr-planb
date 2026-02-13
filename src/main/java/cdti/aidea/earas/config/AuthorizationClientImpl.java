//package cdti.aidea.earas.config;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class AuthorizationClientImpl implements AuthorizationClient {
//
//    private final RestTemplate restTemplate;
//
//    @Value("${useraccess.base-url}")
//    private String userAccessBaseUrl;
//
//    @Override
//    public boolean hasPermission(UUID userId, String permissionCode) {
//
//        String url = userAccessBaseUrl + "/internal/check-permission";
//
//        Map<String, Object> request = new HashMap<>();
//        request.put("userId", userId);
//        request.put("permission", permissionCode);
//
//        ResponseEntity<Boolean> response =
//                restTemplate.postForEntity(url, request, Boolean.class);
//
//        return Boolean.TRUE.equals(response.getBody());
//    }
//}
//
//
