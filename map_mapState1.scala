import org.apache.flink.api.common.state.{MapState, MapStateDescriptor}
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.util.Collector

// Define your user class
case class User(name: String, age: Int)

object FlattenMapStateExample {
  def main(args: Array[String]): Unit = {
    // Set up the execution environment
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    // Define your stream
    val stream: DataStream[(Int, Map[Int, Map[Int, User]])] = env.fromElements(
      (1, Map(1 -> Map(1 -> User("Alice", 30), 2 -> User("Bob", 35))))
    )

    // Define a MapStateDescriptor for the MapState
    val mapStateDescriptor = new MapStateDescriptor[Int, Map[Int, User]](
      "nestedMapState",
      classOf[Int],
      classOf[Map[Int, User]]
    )

    // Apply transformations to flatten the MapState
    val flattenedStream: DataStream[User] = stream.flatMap {
      // Context: (Key, Map[Int, Map[Int, User]])
      case (_, nestedMap) =>
        nestedMap.values.flatMap(_.values) // Flatten the nested maps
    }

    // Print the flattened stream for verification
    flattenedStream.print()

    // Execute the Flink job
    env.execute("Flatten MapState Example")
  }
}
