package cdti.aidea.earas.contract.Projection;

public interface BtrStatsProjection {

    String getLbcode();

    Integer getWet_plot();
    Integer getDry_plot();

    Double getWet_area();
    Double getDry_area();

    Double getTotal_area();
    Integer getTotal_plot();
}