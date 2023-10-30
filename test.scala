import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

public class MyMapStateFunction extends RichFlatMapFunction<Tuple2<String, SomeClass>, Tuple2<String, SomeClass>> {
    private transient MapState<String, SomeClass> mapState;

    @Override
    public void open(Configuration parameters) throws Exception {
        MapStateDescriptor<String, SomeClass> descriptor = new MapStateDescriptor<>(
                "mapState",
                BasicTypeInfo.STRING_TYPE_INFO, // Key type
                TypeInformation.of(SomeClass.class) // Value type
        );
        mapState = getRuntimeContext().getMapState(descriptor);
    }

    @Override
    public void flatMap(Tuple2<String, SomeClass> input, Collector<Tuple2<String, SomeClass>> out) throws Exception {
        String key = input.f0;
        SomeClass value = input.f1;

        // Put the key-value pair into the MapState
        mapState.put(key, value);

        // Process and emit your data here if needed
        out.collect(input);
    }
}

public static void main(String[] args) throws Exception {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

    DataStream<Tuple2<String, SomeClass>> inputDataStream = ...; // Create your input DataStream

    KeyedStream<Tuple2<String, SomeClass>, String> keyedStream = inputDataStream
        .keyBy((KeySelector<Tuple2<String, SomeClass>, String>) value -> value.f0);

    DataStream<Tuple2<String, SomeClass>> resultStream = keyedStream
        .flatMap(new MyMapStateFunction())
        .returns(Types.TUPLE(Types.STRING, TypeInformation.of(SomeClass.class)));

    resultStream.print();

    env.execute("Flink MapState Example");
}
