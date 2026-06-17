package cdti.aidea.earas.utils;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class AgriYearUtil {

    private AgriYearUtil() {
    }

    public static LocalDate getAgriYearStart(String agriYear) {

        String[] years = agriYear.split("-");

        int startYear =
                Integer.parseInt(years[0]);

        return LocalDate.of(
                startYear,
                7,
                1
        );
    }

    public static LocalDate getAgriYearEnd(String agriYear) {
        String[] years = agriYear.split("-");
        int endYear =
                Integer.parseInt(years[1]);

        return LocalDate.of(
                endYear,
                6,
                30
        );
    }

}

//public class AgriYearUtil {
//
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
//}