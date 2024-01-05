import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneOffset}

object DateTimeUtils {

  def trimMilliseconds(dateTimeString: String): Option[String] = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    Try {
      val parsedDateTime = LocalDateTime.parse(dateTimeString, formatter)
      val trimmedDateTime = parsedDateTime.withNano(0)
      val formattedDateTime = formatter.format(trimmedDateTime)
      Some(formattedDateTime)
    }.toOption
  }

  def main(args: Array[String]): Unit = {
    // Example usage
    val dateTimeStringWithMillis = "2022-01-04 12:30:45.123"
    val dateTimeStringWithoutMillis = "2022-01-04 12:30:45"

    val resultWithMillis = trimMilliseconds(dateTimeStringWithMillis)
    val resultWithoutMillis = trimMilliseconds(dateTimeStringWithoutMillis)

    println(s"Result with milliseconds: $resultWithMillis")
    println(s"Result without milliseconds: $resultWithoutMillis")
  }
}
