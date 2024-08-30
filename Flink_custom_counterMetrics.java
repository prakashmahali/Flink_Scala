package org.ap.metrics;

import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.accumulators.LongCounter;
import org.apache.flink.metrics.Counter;

public class CustomerMetrics {
    private Counter flinkCounter;
    private LongCounter accumulatorCounter;

    public CustomerMetrics(RuntimeContext runtimeContext) {
        // Initialize Flink Counter
        this.flinkCounter = runtimeContext.getMetricGroup().counter("flinkCustomCounter");

        // Initialize Accumulator Counter
        this.accumulatorCounter = runtimeContext.getLongCounter("accumulatorCustomCounter");
    }

    // Method to increment the Flink Counter
    public void incrementFlinkCounter() {
        flinkCounter.inc();
    }

    // Method to increment the Accumulator Counter
    public void incrementAccumulatorCounter() {
        accumulatorCounter.add(1);
    }

    // Getters
    public Counter getFlinkCounter() {
        return flinkCounter;
    }

    public LongCounter getAccumulatorCounter() {
        return accumulatorCounter;
    }
}

package org.sp.flink;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.ap.metrics.CustomerMetrics;

public class FlinkMainClass {
    public static void main(String[] args) throws Exception {
        // Set up the execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Example data stream
        DataStream<String> inputStream = env.fromElements("apple", "banana", "cherry");

        // Apply a RichMapFunction that uses CustomerMetrics
        DataStream<String> resultStream = inputStream.map(new RichMapFunction<String, String>() {
            private CustomerMetrics customerMetrics;

            @Override
            public void open(Configuration parameters) throws Exception {
                // Initialize CustomerMetrics with RuntimeContext
                customerMetrics = new CustomerMetrics(getRuntimeContext());
            }

            @Override
            public String map(String value) throws Exception {
                // Increment the custom counters
                customerMetrics.incrementFlinkCounter();
                customerMetrics.incrementAccumulatorCounter();

                // Process the input and return the output
                return "Processed: " + value;
            }
        });

        // Print the result stream
        resultStream.print();

        // Execute the Flink job
        env.execute("Flink Job with Custom Metrics");
    }
}
