package cdti.aidea.earas.contract.Projection;

import java.util.UUID;

public interface ClusterLabelProjection {

    Long getCluDetailId();

    Long getPlotId();

    String getPlotLabel();

    Double getEnumeratedArea();

    Double getTotCent();

    Integer getWardnumber();

    String getOwnername();

    String getAddress();

    String getTpno();

    String getTbsubdivisionno();

    String getOldsvno();

    String getOldsubno();

    String getResvno();

    String getResbdno();

    String getHouseno();

    Long getBTypeId();
}