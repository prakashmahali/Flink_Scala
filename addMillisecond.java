import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class AddMillisecondsToLocalDate {
    public static void main(String[] args) {
        LocalDate localDate = LocalDate.of(2023, 6, 6);
        long millisecondsToAdd = 1000; // 1 second

        LocalDateTime localDateTime = localDate.atStartOfDay();
        localDateTime = localDateTime.plus(millisecondsToAdd, ChronoUnit.MILLIS);

        System.out.println("Original LocalDate: " + localDate);
        System.out.println("LocalDateTime after adding milliseconds: " + localDateTime);
    }
}
