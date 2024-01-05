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


import scala.util.parsing.json.JSON

import scala.util.parsing.json.JSON

object JsonParsingExample {

  def jsonStringToJson(jsonString: String): Option[Any] = {
    JSON.parseFull(jsonString)
  }

  def main(args: Array[String]): Unit = {
    val jsonString = """{"key": "value", "number": 42, "nested": {"innerKey": "innerValue"}}"""

    jsonStringToJson(jsonString) match {
      case Some(json) =>
        println("Parsed JSON:")
        println(json)

      case None =>
        println("Failed to parse JSON.")
    }
  }
}

