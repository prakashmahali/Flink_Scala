import com.amazonaws.services.secretsmanager.AWSSecretsManager
import com.amazonaws.services.secretsmanager.model._

object SecretManagerExample {

  def getSecretPassword(secretName: String, region: String): String = {
    val client: AWSSecretsManager = AWSSecretsManager.builder().region(region).build()

    try {
      val getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName)
      val getSecretValueResponse = client.getSecretValue(getSecretValueRequest)

      if (getSecretValueResponse.secretString() != null) {
        // If the secret value is a string, return it
        getSecretValueResponse.secretString()
      } else {
        // If the secret value is binary, you might need to handle it differently
        throw new RuntimeException("Binary secrets not supported in this example.")
      }
    } catch {
      case e: ResourceNotFoundException =>
        throw new RuntimeException(s"Secret with name $secretName not found.", e)
      case e: InvalidRequestException =>
        throw new RuntimeException(s"Invalid request to retrieve secret $secretName.", e)
      case e: DecryptionFailureException =>
        throw new RuntimeException(s"Decryption failure while retrieving secret $secretName.", e)
      case e: Exception =>
        throw new RuntimeException(s"Error retrieving secret $secretName.", e)
    } finally {
      // Close the AWS Secrets Manager client to release resources
      client.close()
    }
  }

  def main(args: Array[String]): Unit = {
    val secretName = "your-secret-name"
    val region = "your-region"

    try {
      val secretPassword = getSecretPassword(secretName, region)
      println(s"Secret Password: $secretPassword")
    } catch {
      case ex: Exception =>
        println(s"Error: ${ex.getMessage}")
    }
  }
}
