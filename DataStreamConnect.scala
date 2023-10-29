import org.apache.flink.api.common.state.{MapState, MapStateDescriptor}
import org.apache.flink.api.common.functions.RichCoFlatMapFunction
import org.apache.flink.configuration.Configuration
import org.apache.flink.util.Collector

case class MyData(name: String, age: Int)

class MyCoFlatMapFunction extends RichCoFlatMapFunction[String, (String, MyData), (String, MyData)] {
  private var mapState: MapState[String, MyData] = _

  override def open(parameters: Configuration): Unit = {
    val mapStateDescriptor = new MapStateDescriptor[String, MyData]("myMapState", classOf[String], classOf[MyData])
    mapState = getRuntimeContext.getMapState(mapStateDescriptor)
  }

  override def flatMap1(input1: String, out: Collector[(String, MyData)]): Unit = {
    // Process input1 and emit output based on the map state
    if (mapState.contains(input1)) {
      val data = mapState.get(input1)
      out.collect((input1, data))
    }
  }

  override def flatMap2(input2: (String, MyData), out: Collector[(String, MyData)]): Unit = {
    // Update the map state with input2
    val key = input2._1
    val data = input2._2
    mapState.put(key, data)
  }
}
