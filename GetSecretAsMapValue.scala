import io.circe.Json
import io.circe.parser._

object AwsSecretsManagerUtils {

  def jsonStringToMap(jsonString: String): Map[String, String] = {
    parse(jsonString) match {
      case Right(json) =>
        json.asObject.flatMap(_.toMap.mapValues(_.asString.getOrElse(""))).getOrElse(Map.empty)
      case Left(_) =>
        Map.empty
    }
  }

  def main(args: Array[String]): Unit = {
    // Example usage
    val jsonString =
      """
        |{
        |  "username": "your_username",
        |  "password": "your_password"
        |}
        |""".stripMargin

    val secretMap: Map[String, String] = jsonStringToMap(jsonString)

    // Use the values from the map as needed
    val username = secretMap.getOrElse("username", "")
    val password = secretMap.getOrElse("password", "")

    println(s"Username: $username")
    println(s"Password: $password")
  }
}
