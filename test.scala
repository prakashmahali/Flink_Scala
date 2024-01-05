import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse
import scala.util.{Try, Success, Failure}

object AwsSecretsManagerExample {

  def getUsernamePasswordFromSecret(response: GetSecretValueResponse): (String, String) = {
    val secretString: String = response.secretString()

    val username = Try(JsonParser(secretString).asJsObject.fields("username").convertTo[String]) match {
      case Success(value) => value
      case Failure(_) => ""
    }

    val password = Try(JsonParser(secretString).asJsObject.fields("password").convertTo[String]) match {
      case Success(value) => value
      case Failure(_) => ""
    }

    (username, password)
  }

  def main(args: Array[String]): Unit = {
    // Assuming you have the GetSecretValueResponse instance, replace this with your actual response.
    val response: GetSecretValueResponse = ???

    val (username, password) = getUsernamePasswordFromSecret(response)

    println(s"Username: $username")
    println(s"Password: $password")
  }
}
