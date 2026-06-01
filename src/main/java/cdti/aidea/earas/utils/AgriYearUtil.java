package cdti.aidea.earas.utils;

import java.time.LocalDate;

public class AgriYearUtil {

//    public static LocalDate getAgriYearStart(LocalDate today) {
//        if (today.getMonthValue() >= 6) {
//            return LocalDate.of(today.getYear(), 6, 1);
//        } else {
//            return LocalDate.of(today.getYear() - 1, 6, 1);
//        }
//    }
//
//    public static LocalDate getAgriYearEnd(LocalDate today) {
//        if (today.getMonthValue() >= 6) {
//            return LocalDate.of(today.getYear() + 1, 7, 31);
//        } else {
//            return LocalDate.of(today.getYear(), 7, 31);
//        }
//    }

// Agri Year Start -> July 1
// Returns start date -> 2025-07-01
public static LocalDate getAgriStartDate(String agriYear) {

    String[] years = agriYear.split("-");

    int startYear = Integer.parseInt(years[0]);

    return LocalDate.of(startYear, 7, 1);
}

    // Returns end date -> 2026-06-30
    public static LocalDate getAgriEndDate(String agriYear) {

        String[] years = agriYear.split("-");

        int endYear = Integer.parseInt(years[1]);

        return LocalDate.of(endYear, 6, 30);
    }

}