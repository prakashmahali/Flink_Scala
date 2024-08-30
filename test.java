public class OrderDetails {
    private String orderId;
    private String custId;
    private String timestamp;

    // Constructors, Getters, Setters
    public OrderDetails(String orderId, String custId, String timestamp) {
        this.orderId = orderId;
        this.custId = custId;
        this.timestamp = timestamp;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustId() {
        return custId;
    }

    public String getTimestamp() {
        return timestamp;
    }
}

public class OrderOutput {
    private String orderId;
    private String custId;
    private String timestamp;
    private long countOfCustomer;

    // Constructors, Getters, Setters
    public OrderOutput(String orderId, String custId, String timestamp, long countOfCustomer) {
        this.orderId = orderId;
        this.custId = custId;
        this.timestamp = timestamp;
        this.countOfCustomer = countOfCustomer;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustId() {
        return custId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public long getCountOfCustomer() {
        return countOfCustomer;
    }
}
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple2;

public class CustomerCountAggregator implements AggregateFunction<OrderDetails, Tuple2<Long, Set<String>>, Long> {

    @Override
    public Tuple2<Long, Set<String>> createAccumulator() {
        return Tuple2.of(0L, new HashSet<>());
    }

    @Override
    public Tuple2<Long, Set<String>> add(OrderDetails value, Tuple2<Long, Set<String>> accumulator) {
        Set<String> custIds = accumulator.f1;
        custIds.add(value.getCustId());
        return Tuple2.of((long) custIds.size(), custIds);
    }

    @Override
    public Long getResult(Tuple2<Long, Set<String>> accumulator) {
        return accumulator.f0;
    }

    @Override
    public Tuple2<Long, Set<String>> merge(Tuple2<Long, Set<String>> a, Tuple2<Long, Set<String>> b) {
        Set<String> mergedCustIds = new HashSet<>(a.f1);
        mergedCustIds.addAll(b.f1);
        return Tuple2.of((long) mergedCustIds.size(), mergedCustIds);
    }
}
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;

public class OrderMetricsMapFunction extends RichMapFunction<Long, OrderOutput> {

    private transient Counter customerCounter;

    @Override
    public void open(Configuration parameters) throws Exception {
        // Register a Flink counter
        customerCounter = getRuntimeContext().getMetricGroup().counter("customerCounter");
    }

    @Override
    public OrderOutput map(Long count) throws Exception {
        // Increment the Flink counter with the count value
        customerCounter.inc(count);
        // Assuming orderId, custId, and timestamp are constant or derived elsewhere
        return new OrderOutput("N/A", "N/A", "N/A", count);
    }
}
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;

public class OrderProcessingJob {

    public static void main(String[] args) throws Exception {
        // Set up the execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Create a DataStream of OrderDetails elements (this would normally come from a source like Kafka)
        DataStream<OrderDetails> orderDataStream = env.fromElements(
                new OrderDetails("order1", "cust1", "2024-08-29T12:00:00"),
                new OrderDetails("order2", "cust2", "2024-08-29T12:05:00"),
                new OrderDetails("order3", "cust1", "2024-08-29T12:10:00")
                // Add more OrderDetails instances as needed
        );

        // Apply windowing and aggregation
        DataStream<Long> aggregatedStream = orderDataStream
            .keyBy(OrderDetails::getOrderId) // or keyBy(OrderDetails::getCustId) depending on the requirement
            .window(TumblingProcessingTimeWindows.of(Time.minutes(15)))
            .aggregate(new CustomerCountAggregator());

        // Apply the custom metrics function
        DataStream<OrderOutput> resultStream = aggregatedStream.map(new OrderMetricsMapFunction());

        // Print the output stream
        resultStream.print();

        // Execute the Flink job
        env.execute("Order Processing with Custom Metrics");
    }
}
