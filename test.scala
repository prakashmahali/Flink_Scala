import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZonedDateTime}
import scala.util.Try

object DateTimeUtils {

  def convertToStandardFormat(dateTimeString: String): Option[String] = {
    val supportedFormats = List(
      "yyyy-MM-dd HH:mm:ss",
      "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
      "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
      "yyyyMMddHHmmss"
      // Add more formats as needed
    )

    val formatter = supportedFormats.view.map(DateTimeFormatter.ofPattern)

    val result = formatter.flatMap { fmt =>
      Try {
        var updatedDateTimeString = dateTimeString
        if (!updatedDateTimeString.contains(":ss")) {
          // If ":ss" is missing, add seconds as ":00"
          updatedDateTimeString = updatedDateTimeString + ":00"
        }

        val parsedDateTime = LocalDateTime.parse(updatedDateTimeString, fmt)
        Some(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(parsedDateTime))
      }.toOption
    }

    result.headOption
  }

  def main(args: Array[String]): Unit = {
    // Example usage
    val dateTimeString1 = "2022-01-04 12:30:45"
    val dateTimeString2 = "2022-01-04T12:30:45.123Z"
    val dateTimeString3 = "2022-01-04T12:30:45+0100"
    val dateTimeString4 = "202201041230"

    val result1 = convertToStandardFormat(dateTimeString1)
    val resul
