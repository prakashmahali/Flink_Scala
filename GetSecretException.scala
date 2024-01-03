import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.{GetSecretValueRequest, GetSecretValueResponse}

import scala.util.{Failure, Success, Try}

object SecretManagerExample {

  def getSecretPassword(secretName: String, region: String): String = {
    val client: SecretsManagerClient = SecretsManagerClient.builder().region(region).build()

    val request: GetSecretValueRequest = GetSecretValueRequest.builder().secretId(secretName).build()

    val response: Try[GetSecretValueResponse] = Try(client.getSecretValue(request))

    response match {
      case Success(secretValueResponse) =>
        Option(secretValueResponse.secretString()) match {
          case Some(secretString) => secretString
          case None => throw new RuntimeException("Secret string is null or empty")
        }

      case Failure(exception) =>
        throw new RuntimeException(s"Failed to retrieve secret: ${exception.getMessage}")
    }
  }

  def main(args: Array[String]): Unit = {
    val secretName = "your-secret-name"
    val region = "your-aws-region"

    try {
      val secretPassword = getSecretPassword(secretName, region)
      println(s"Secret Password: $secretPassword")
    } catch {
      case e: Exception => println(s"Error: ${e.getMessage}")
    }
  }
}
