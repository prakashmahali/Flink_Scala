import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.metrics.Counter;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.HashMap;
import java.util.Map;

public class MarketIdCounterExample {

    public static void main(String[] args) throws Exception {
        // Set up the streaming execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Create a simple input data stream (Example: marketId, downloadSpeed, uploadSpeed, eventTime)
        DataStream<InputRecord> input = env.fromElements(
                new InputRecord("market1", 50.5, 20.3, "2024-09-02T10:00:00"),
                new InputRecord("market2", 70.1, 30.2, "2024-09-02T10:01:00"),
                new InputRecord("market1", 60.7, 25.1, "2024-09-02T10:02:00")
        );

        // Apply the RichMapFunction with counter metric for marketId
        DataStream<InputRecord> processedStream = input.map(new MarketIdCounterRichMapFunction());

        // Print the processed stream
        processedStream.print();

        // Execute the job
        env.execute("Flink MarketId Counter Example");
    }

    // Define the input record class
    public static class InputRecord {
        public String marketId;
        public double downloadSpeed;
        public double uploadSpeed;
        public String eventTime;

        public InputRecord() {}

        public InputRecord(String marketId, double downloadSpeed, double uploadSpeed, String eventTime) {
            this.marketId = marketId;
            this.downloadSpeed = downloadSpeed;
            this.uploadSpeed = uploadSpeed;
            this.eventTime = eventTime;
        }

        @Override
        public String toString() {
            return "InputRecord{" +
                    "marketId='" + marketId + '\'' +
                    ", downloadSpeed=" + downloadSpeed +
                    ", uploadSpeed=" + uploadSpeed +
                    ", eventTime='" + eventTime + '\'' +
                    '}';
        }
    }

    // Define the RichMapFunction to count records per marketId
    public static class MarketIdCounterRichMapFunction extends RichMapFunction<InputRecord, InputRecord> {

        // Map to store counters for each marketId
        private transient Map<String, Counter> marketIdCounters;

        @Override
        public void open(Configuration parameters) {
            // Initialize the map of counters
            this.marketIdCounters = new HashMap<>();
        }

        @Override
        public InputRecord map(InputRecord value) {
            // Get the marketId from the input record
            String marketId = value.marketId;

            // Get or create the counter for the specific marketId
            Counter counter = marketIdCounters.computeIfAbsent(marketId, id -> 
                getRuntimeContext().getMetricGroup().counter("marketId_" + id + "_counter")
            );

            // Increment the counter
            counter.inc();

            // Return the input record as is (or modify it if needed)
            return value;
        }

        @Override
        public void close() {
            // Cleanup logic if needed
        }
    }
}
