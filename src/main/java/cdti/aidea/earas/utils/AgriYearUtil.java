package cdti.aidea.earas.utils;

import java.time.LocalDate;

public class AgriYearUtil {

    public static LocalDate getAgriYearStart(LocalDate today) {
        if (today.getMonthValue() >= 6) {
            return LocalDate.of(today.getYear(), 6, 1);
        } else {
            return LocalDate.of(today.getYear() - 1, 6, 1);
        }
    }

    public static LocalDate getAgriYearEnd(LocalDate today) {
        if (today.getMonthValue() >= 6) {
            return LocalDate.of(today.getYear() + 1, 7, 31);
        } else {
            return LocalDate.of(today.getYear(), 7, 31);
        }
    }
}