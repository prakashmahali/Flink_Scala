package org.ap.metrics;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.common.accumulators.LongCounter;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Counter;
import org.sp.flink.CustomerInput;
import org.sp.flink.CustomerOutput;

public class CustomerMetrics extends RichMapFunction<CustomerInput, CustomerOutput> {

    // Define a Flink Counter
    private transient Counter counter;

    @Override
    public void open(Configuration parameters) throws Exception {
        // Initialize the counter in the open method
        counter = getRuntimeContext().getMetricGroup().counter("processedElementsCounter");
    }

    @Override
    public CustomerOutput map(CustomerInput input) throws Exception {
        // Increment the counter each time an element is processed
        counter.inc();

        // Process the input and return the output
        return new CustomerOutput(input.getId(), "Processed: " + input.getName());
    }
}
package org.sp.flink;

public class CustomerInput {
    private int id;
    private String name;

    public CustomerInput(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

package org.sp.flink;

public class CustomerOutput {
    private int id;
    private String processedName;

    public CustomerOutput(int id, String processedName) {
        this.id = id;
        this.processedName = processedName;
    }

    public int getId() {
        return id;
    }

    public String getProcessedName() {
        return processedName;
    }
}

package org.sp.flink;

import org.ap.metrics.CustomerMetrics;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class FlinkJob {
    public static void main(String[] args) throws Exception {
        // Set up the execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Create a DataStream of CustomerInput elements
        DataStream<CustomerInput> inputStream = env.fromElements(
            new CustomerInput(1, "Alice"),
            new CustomerInput(2, "Bob"),
            new CustomerInput(3, "Charlie")
        );

        // Apply the CustomerMetrics function
        DataStream<CustomerOutput> outputStream = inputStream.map(new CustomerMetrics());

        // Print the result stream
        outputStream.print();

        // Execute the Flink job
        env.execute("Flink Custom Counter Example");
    }
}

