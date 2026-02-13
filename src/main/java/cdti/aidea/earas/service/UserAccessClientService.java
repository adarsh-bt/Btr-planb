package cdti.aidea.earas.service;

import cdti.aidea.earas.config.UserAccessClient;
import cdti.aidea.earas.contract.UserAccessDTOs.UserLoginDetailsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAccessClientService {

private final UserAccessClient userAccessClient;

 public UserLoginDetailsResponse getUserDetails(UUID loginId){

     return userAccessClient.getUserDetailsByLoginId(loginId);
 }

}
