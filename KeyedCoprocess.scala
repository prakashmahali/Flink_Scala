import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction;
import org.apache.flink.util.Collector;

public class OrderPaymentMatchFunction extends KeyedCoProcessFunction<String, OrderEvent, PaymentEvent, String> {

    private ValueState<OrderEvent> orderState;
    private ValueState<PaymentEvent> paymentState;

    @Override
    public void open(Configuration parameters) throws Exception {
        orderState = getRuntimeContext().getState(new ValueStateDescriptor<>("orderState", OrderEvent.class));
        paymentState = getRuntimeContext().getState(new ValueStateDescriptor<>("paymentState", PaymentEvent.class));
    }

    @Override
    public void processElement1(OrderEvent order, Context ctx, Collector<String> out) throws Exception {
        PaymentEvent payment = paymentState.value();
        if (payment != null) {
            out.collect("Order " + order.orderId + " has been matched with payment " + payment.paymentId);
            paymentState.clear();
        } else {
            orderState.update(order);
            ctx.timerService().registerProcessingTimeTimer(ctx.timerService().currentProcessingTime() + 60000); // 1 minute
        }
    }

    @Override
    public void processElement2(PaymentEvent payment, Context ctx, Collector<String> out) throws Exception {
        OrderEvent order = orderState.value();
        if (order != null) {
            out.collect("Order " + order.orderId + " has been matched with payment " + payment.paymentId);
            orderState.clear();
        } else {
            paymentState.update(payment);
            ctx.timerService().registerProcessingTimeTimer(ctx.timerService().currentProcessingTime() + 60000); // 1 minute
        }
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
        OrderEvent order = orderState.value();
        PaymentEvent payment = paymentState.value();

        if (order != null && payment == null) {
            out.collect("Order " + order.orderId + " did not receive a payment within the expected time.");
            orderState.clear();
        }
        
        if (payment != null && order == null) {
            // this scenario might not happen if payment has no corresponding order (optional handling)
            paymentState.clear();
        }
    }
}


import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class FlinkJob {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Assuming orderStream and paymentStream are already defined and obtained
        KeyedStream<OrderEvent, String> keyedOrderStream = orderStream.keyBy(order -> order.orderId);
        KeyedStream<PaymentEvent, String> keyedPaymentStream = paymentStream.keyBy(payment -> payment.orderId);

        SingleOutputStreamOperator<String> resultStream = keyedOrderStream
            .connect(keyedPaymentStream)
            .process(new OrderPaymentMatchFunction());

        resultStream.print();

        env.execute("Order Payment Matching Job");
    }
}
