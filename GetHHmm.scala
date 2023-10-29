import java.sql.Timestamp
import java.text.SimpleDateFormat

def extractHHMMFromTimestamp(timestamp: Timestamp): String = {
  val dateFormat = new SimpleDateFormat("HH:mm")
  val formattedTime = dateFormat.format(timestamp)
  formattedTime
}

// Example usage:
val timestamp = Timestamp.valueOf("2023-10-28 14:30:00")
val time = extractHHMMFromTimestamp(timestamp)
println(time)
