import org.apache.flink.api.common.functions.AggregateFunction;

public class MarketIdCountAggregateFunction implements AggregateFunction<MarketData, Long, Long> {

    @Override
    public Long createAccumulator() {
        return 0L;
    }

    @Override
    public Long add(MarketData value, Long accumulator) {
        // Increment the count for each marketId (assuming one occurrence per record)
        return accumulator + 1;
    }

    @Override
    public Long getResult(Long accumulator) {
        return accumulator;
    }

    @Override
    public Long merge(Long a, Long b) {
        return a + b;
    }
}

import org.apache.flink.api.common.functions.RichCoFlatMapFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;
import org.apache.flink.util.Collector;

public class MarketIdCountRichCoFlatMapFunction extends RichCoFlatMapFunction<MarketData, MarketData, MarketData> {

    private transient ValueState<Long> countState;
    private transient Counter marketIdCounter;

    @Override
    public void open(Configuration parameters) throws Exception {
        countState = getRuntimeContext().getState(new ValueStateDescriptor<>("marketIdCount", Long.class, 0L));
        marketIdCounter = getRuntimeContext().getMetricGroup().counter("marketIdCounter");
    }

    @Override
    public void flatMap1(MarketData value, Collector<MarketData> out) throws Exception {
        Long currentCount = countState.value();
        countState.update(currentCount + 1);
        marketIdCounter.inc();
        out.collect(new MarketData(value.getMarketId(), currentCount + 1)); // Emit with updated count
    }

    @Override
    public void flatMap2(MarketData value, Collector<MarketData> out) throws Exception {
        // No-op for the second input if not used
    }
}
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;

public class MarketDataProcessingJob {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Sample data stream (typically this would come from a source like Kafka)
        DataStream<MarketData> marketDataStream = env.fromElements(
                new MarketData("market1", System.currentTimeMillis()),
                new MarketData("market2", System.currentTimeMillis()),
                new MarketData("market1", System.currentTimeMillis()) // Example data
                // Add more MarketData instances as needed
        );

        // Apply keyBy, window, and aggregation
        DataStream<Long> aggregatedStream = marketDataStream
            .keyBy(MarketData::getMarketId) // Key by marketId
            .window(TumblingProcessingTimeWindows.of(Time.minutes(15))) // 15-minute window
            .aggregate(new MarketIdCountAggregateFunction()); // Aggregate to count

        // Apply RichCoFlatMapFunction for custom metrics
        DataStream<MarketData> resultStream = marketDataStream
            .connect(marketDataStream) // Connect with itself for this example
            .flatMap(new MarketIdCountRichCoFlatMapFunction());

        // Print the result stream
        resultStream.print();

        env.execute("Market Data Processing with Custom Metrics");
    }
}
