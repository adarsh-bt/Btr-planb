package cdti.aidea.earas.contract.Response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
//@AllArgsConstructor
@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ZoneListResponse {
  private int zoneId;
  private Integer zoneCode;
  private String zoneNameEn;
  private String zoneNameMal;
  private String zoneType;
  private int desTalukId;
  private int desDistId;
  private String talukName;
  private String districtName;

  public ZoneListResponse(int zoneId, Integer zoneCode, String zoneNameEn, String zoneNameMal,String zoneType,
                          int desTalukId, int desDistId, String talukName, String districtName) {
    this.zoneId = zoneId;
    this.zoneCode = zoneCode;
    this.zoneNameEn = zoneNameEn;
    this.zoneNameMal = zoneNameMal;
    this.zoneType = zoneType;
    this.desTalukId = desTalukId;
    this.desDistId = desDistId;
    this.talukName = talukName;
    this.districtName = districtName;
  }

  // Getters and setters (or use Lombok @Data if preferred)
}
