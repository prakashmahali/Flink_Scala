import java.time.{LocalDate, LocalDateTime, ZoneId, Duration}

object AddMillisecondsExample extends App {
  // Define a LocalDate
  val localDate: LocalDate = LocalDate.of(2024, 6, 6)

  // Convert LocalDate to LocalDateTime at the start of the day
  val localDateTime: LocalDateTime = localDate.atStartOfDay()

  // Define the number of milliseconds to add
  val millisecondsToAdd: Long = 5000 // example: 5000 milliseconds (5 seconds)

  // Add the milliseconds to the LocalDateTime
  val updatedDateTime: LocalDateTime = localDateTime.plus(Duration.ofMillis(millisecondsToAdd))

  // Print the results
  println(s"Original LocalDateTime: $localDateTime")
  println(s"Updated LocalDateTime: $updatedDateTime")
}
