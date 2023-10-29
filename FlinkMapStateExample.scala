import org.apache.flink.api.common.state.{MapState, MapStateDescriptor}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector

object FlinkMapStateExample {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val stream1: DataStream[(String, String)] = env.fromElements(
      ("key1", "value1"),
      ("key2", "value2"),
      ("key1", "value3")
    )

    val stream2: DataStream[(String, HashMap[String, String])] = env.fromElements(
      ("key1", HashMap("a" -> "apple", "b" -> "banana")),
      ("key2", HashMap("c" -> "cherry"))
    )

    val mapStateDescriptor = new MapStateDescriptor[String, HashMap[String, String]](
      "hashMapState",
      TypeInformation.of(classOf[String]),
      TypeInformation.of(classOf[HashMap[String, String]])
    )

    val result: DataStream[String] = stream1
      .keyBy(_._1)
      .connect(stream2.keyBy(_._1))
      .process(new MyKeyedBroadcastProcessFunction(mapStateDescriptor))

    result.print()

    env.execute("Flink MapState Example")
  }

  class MyKeyedBroadcastProcessFunction(mapStateDescriptor: MapStateDescriptor[String, HashMap[String, String]])
    extends KeyedBroadcastProcessFunction[String, (String, String), (String, HashMap[String, String]), String] {

    override def processElement(value: (String, String),
                                ctx: KeyedBroadcastProcessFunction[String, (String, String), (String, HashMap[String, String]), String]#ReadOnlyContext,
                                out: Collector[String]): Unit = {

      val key = value._1
      val newValue = value._2

      val broadcastState = ctx.getBroadcastState(mapStateDescriptor)
      val stateForCurrentKey = broadcastState.get(key)

      if (stateForCurrentKey != null) {
        stateForCurrentKey.put(key, newValue)
        broadcastState.put(key, stateForCurrentKey)
      }
    }

    override def processBroadcastElement(value: (String, HashMap[String, String]),
                                         ctx: KeyedBroadcastProcessFunction[String, (String, String), (String, HashMap[String, String]), String]#Context,
                                         out: Collector[String]): Unit = {
      val key = value._1
      val hashMap = value._2

      val broadcastState = ctx.getBroadcastState(mapStateDescriptor)
      broadcastState.put(key, hashMap)
    }
  }
}
