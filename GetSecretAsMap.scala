import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.{GetSecretValueRequest, GetSecretValueResponse}
import scala.jdk.CollectionConverters._

object AwsSecretsManagerExample {

  def getSecretAsMap(secretName: String, region: String): Map[String, String] = {
    val client = SecretsManagerClient.builder.region(region).build()

    try {
      val request = GetSecretValueRequest.builder.secretId(secretName).build()
      val response: GetSecretValueResponse = client.getSecretValue(request)

      val secretString: String = response.secretString()

      // Parse the secret string as JSON and convert it to a Map
      val secretMap: Map[String, String] = io.circe.parser.parse(secretString) match {
        case Right(json) => json.asObject.flatMap(_.toMap.mapValues(_.asString.getOrElse(""))).getOrElse(Map.empty)
        case Left(_) => Map.empty
      }

      secretMap
    } finally {
      client.close()
    }
  }

  def main(args: Array[String]): Unit = {
    val secretName = "your-secret-name"
    val region = "your-aws-region"

    val secretMap: Map[String, String] = getSecretAsMap(secretName, region)

    // Use the values from the map as needed
    val username = secretMap.getOrElse("username", "")
    val password = secretMap.getOrElse("password", "")

    println(s"Username: $username")
    println(s"Password: $password")
  }
}
