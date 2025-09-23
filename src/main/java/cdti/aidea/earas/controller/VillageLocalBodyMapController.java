package cdti.aidea.earas.controller;

import cdti.aidea.earas.model.Btr_models.VillageLocalBodyMap;
import cdti.aidea.earas.service.VillageLocalBodyMapService;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/village-localbody")
@RequiredArgsConstructor
public class VillageLocalBodyMapController {
  private final VillageLocalBodyMapService villageLocalBodyMapService;

  @PostMapping("/link")
  public ResponseEntity<VillageLocalBodyMap> linkVillageToLocalbody(
      @RequestBody LinkRequest request) {
    VillageLocalBodyMap map =
        villageLocalBodyMapService.linkVillageToLocalbody(
            request.getVillageId(), request.getLocalbodyId());
    return ResponseEntity.ok(map);
  }

  // @PostMapping("/link")
  // public ResponseEntity<List<VillageLocalBodyMap>> linkVillageToLocalbody(@RequestBody
  // List<LinkRequest> requests) {
  //    List<VillageLocalBodyMap> maps = requests.stream()
  //            .map(r -> villageLocalBodyMapService.linkVillageToLocalbody(r.getVillageId(),
  // r.getLocalbodyId()))
  //            .collect(Collectors.toList());
  //    return ResponseEntity.ok(maps);
  // }

  @Data
  public static class LinkRequest {
    @NotNull(message = "Village ID must not be null")
    private Integer villageId;

    @NotNull(message = "LocalBody ID must not be null")
    private Integer localbodyId;
  }
}
