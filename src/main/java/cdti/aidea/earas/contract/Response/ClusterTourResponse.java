package cdti.aidea.earas.contract.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClusterTourResponse {
    private Integer clusterNo;
    private String zoneName;
    private String landType;
    private String localbody;
    private String block;
    private String ownername;
    private String address;
    private String number;
    private String districtName;
    private String talukName;
    private List<String> clusterLabels;
}