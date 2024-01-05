libraryDependencies += "com.typesafe.play" %% "play-json" % "2.10.4"
import play.api.libs.json._

object JsonExample {
  def jsonStringToJson(jsonString: String): JsValue = {
    Json.parse(jsonString)
  }

  def main(args: Array[String]): Unit = {
    val jsonString = """{"key": "value", "number": 42, "nested": {"innerKey": "innerValue"}}"""

    val json: JsValue = jsonStringToJson(jsonString)

    // You can now work with the JSON object
    val key = (json \ "key").as[String]
    val number = (json \ "number").as[Int]
    val innerValue = (json \ "nested" \ "innerKey").as[String]

    println(s"Key: $key")
    println(s"Number: $number")
    println(s"Inner Value: $innerValue")
  }
}
