import org.apache.flink.api.common.state.{MapState, MapStateDescriptor}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.datastream.DataStream
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.util.Collector

case class Record(key: String, value: String)

object GroupByKeyValue {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1) // Set your desired parallelism

    val dataStream: DataStream[Record] = env.fromElements(
      Record("key1", "value1"),
      Record("key2", "value2"),
      Record("key1", "value3"),
      Record("key3", "value4")
    )

    val groupedStream: DataStream[Record] = dataStream
      .keyBy(_.key)
      .flatMap(new GroupByKeyValueFunction)

    groupedStream.print()

    env.execute("GroupByKeyValueExample")
  }
}

class GroupByKeyValueFunction extends RichFlatMapFunction[Record, Record] {
  private var state: MapState[String, List[String]] = _

  override def open(parameters: Configuration): Unit = {
    val stateDescriptor = new MapStateDescriptor[String, List[String]]("keyValueState", TypeInformation.of(classOf[String]), TypeInformation.of(classOf[List[String]])
    state = getRuntimeContext.getMapState(stateDescriptor)
  }

  override def flatMap(input: Record, out: Collector[Record]): Unit = {
    val key = input.key
    val value = input.value

    // Get the current list of values for the key, or create a new list if it doesn't exist
    var valuesList = state.get(key)
    if (valuesList == null) {
      valuesList = List()
    }

    // Append the new value to the list
    valuesList = value :: valuesList

    // Update the state with the new list
    state.put(key, valuesList)

    // Emit the grouped records
    out.collect(Record(key, valuesList.mkString(", ")))
  }
}
