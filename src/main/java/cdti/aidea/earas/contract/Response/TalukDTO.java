package cdti.aidea.earas.contract.Response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class TalukDTO {

    private Long talukMappingId;
    private Integer talukId;
    private String talukName;

    private List<VillageDTO> villages = new ArrayList<>();

    public TalukDTO(Long talukMappingId, Integer talukId, String talukName) {
        this.talukMappingId = talukMappingId;
        this.talukId = talukId;
        this.talukName = talukName;
    }

    public TalukDTO(Integer talukId, String talukName) {
        this.talukId = talukId;
        this.talukName = talukName;
    }
}