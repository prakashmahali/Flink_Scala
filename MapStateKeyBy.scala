import org.apache.flink.api.common.state.{MapState, MapStateDescriptor}
import org.apache.flink.api.common.typeinfo.{TypeInformation, Types}
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.datastream.DataStream
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment
import org.apache.flink.util.Collector

case class Student(className: String, section: String, name: String)

object CombineStudentNames {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1) // Set your desired parallelism

    val dataStream: DataStream[Student] = env.fromElements(
      Student("Math", "A", "Alice"),
      Student("Math", "B", "Bob"),
      Student("English", "A", "Charlie"),
      Student("Math", "A", "David")
    )

    val combinedStream: DataStream[(String, String)] = dataStream
      .keyBy(_.className)
      .flatMap(new CombineStudentNamesFunction)

    combinedStream.print()

    env.execute("CombineStudentNamesExample")
  }
}

class CombineStudentNamesFunction extends RichFlatMapFunction[Student, (String, String)] {
  private var state: MapState[String, Student] = _

  override def open(parameters: Configuration): Unit = {
    val stateDescriptor = new MapStateDescriptor[String, Student]("studentState", Types.STRING, TypeInformation.of(classOf[Student]))
    state = getRuntimeContext.getMapState(stateDescriptor)
  }

  override def flatMap(input: Student, out: Collector[(String, String)]): Unit = {
    val className = input.className
    val section = input.section
    val name = input.name

    val student = state.get(className)
    if (student == null) {
      state.put(className, input)
    } else {
      val updatedStudent = Student(className, student.section + ", " + section, student.name + ", " + name)
      state.put(className, updatedStudent)
    }

    out.collect((className, state.get(className).section + " - " + state.get(className).name))
  }
}
