import org.apache.flink.api.common.state.{MapState, MapStateDescriptor, ValueState, ValueStateDescriptor}
import org.apache.flink.streaming.api.functions.co.RichCoFlatMapFunction
import org.apache.flink.util.Collector

// Define the classes
case class User1(id: Int, name: String, age: Int, comment_code: Int, sequence_num: Int)
case class User2(id: Int, name: String, age: Int)

class JoinAndUpdateFunction extends RichCoFlatMapFunction[User1, User2, (Int, Map[Int, User1], User2)] {

  // Define MapState for User1
  private var user1MapState: MapState[Int, Map[Int, User1]] = _

  // Define ValueState for User2
  private var user2ValueState: ValueState[User2] = _

  override def open(parameters: org.apache.flink.configuration.Configuration): Unit = {
    // Initialize MapState for User1
    val user1MapDescriptor = new MapStateDescriptor[Int, Map[Int, User1]](
      "user1MapState",
      classOf[Int],
      classOf[Map[Int, User1]]
    )
    user1MapState = getRuntimeContext.getMapState(user1MapDescriptor)

    // Initialize ValueState for User2
    val user2ValueDescriptor = new ValueStateDescriptor[User2](
      "user2ValueState",
      classOf[User2]
    )
    user2ValueState = getRuntimeContext.getState(user2ValueDescriptor)
  }

  override def flatMap1(user1: User1, out: Collector[(Int, Map[Int, User1], User2)]): Unit = {
    // Retrieve the map for the given comment_code from state or create a new one if it doesn't exist
    val user1Map = Option(user1MapState.get(user1.comment_code)).getOrElse(Map.empty[Int, User1])

    // Update the map with the new value
    val updatedUser1Map = user1Map + (user1.sequence_num -> user1)

    // Update the state with the modified map
    user1MapState.put(user1.comment_code, updatedUser1Map)

    // Emit the result
    out.collect((user1.id, updatedUser1Map, user2ValueState.value()))
  }

  override def flatMap2(user2: User2, out: Collector[(Int, Map[Int, User1], User2)]): Unit = {
    // Update the value state with the new value
    user2ValueState.update(user2)

    // Emit the result
    out.collect((user2.id, Map.empty, user2))
  }
}
