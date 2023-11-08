val maybeValue: Option[Int] = Some(42) // A Some with a value
val maybeEmpty: Option[Int] = None // A None, representing no value

// You can use pattern matching with Option to extract values safely
maybeValue match {
  case Some(value) => println(s"Got a value: $value")
  case None => println("No value found")
}

// You can also use methods like `getOrElse` to provide a default value if None
val result = maybeEmpty.getOrElse(0)
println(s"Result: $result") // This will print "Result: 0"
