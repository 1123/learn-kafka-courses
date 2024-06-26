package io.confluent.developer.serdes.solution;

import io.confluent.developer.StreamsUtils;
import io.confluent.developer.avro.ProcessedOrder;
import io.confluent.developer.avro.ProductOrder;
import io.confluent.developer.serdes.SRTopicLoader;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import static io.confluent.developer.StreamsUtils.prefixedTopicName;

public class StreamsSerdesSchemaRegistry {

    public static void main(String[] args) throws IOException {
        final Properties streamsProps = StreamsUtils.loadProperties();
        StreamsUtils.setApplicationID(streamsProps, "schema-registry-streams");

        StreamsBuilder builder = new StreamsBuilder();
        final String inputTopic = prefixedTopicName(streamsProps.getProperty("sr.input.topic"));
        final String outputTopic = prefixedTopicName(streamsProps.getProperty("sr.output.topic"));
        final Map<String, String> configMap = propertiesToMap(streamsProps);
        final SpecificAvroSerde<ProductOrder> productOrderSerde = getSpecificAvroSerde(configMap);
        final SpecificAvroSerde<ProcessedOrder> processedOrderSerde = getSpecificAvroSerde(configMap);

        final KStream<String, ProductOrder> orderStream = builder.stream(inputTopic, Consumed.with(Serdes.String(), productOrderSerde));
        orderStream.mapValues(value -> ProcessedOrder.newBuilder()
                        .setProduct(value.getProduct())
                        .setTimeProcessed(Instant.now().toEpochMilli()).build())
                .to(outputTopic, Produced.with(Serdes.String(), processedOrderSerde));

        try (KafkaStreams kafkaStreams = new KafkaStreams(builder.build(), streamsProps)) {
            final CountDownLatch shutdownLatch = new CountDownLatch(1);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                kafkaStreams.close(Duration.ofSeconds(2));
                shutdownLatch.countDown();
            }));
            SRTopicLoader.runProducer();
            try {
                kafkaStreams.start();
                shutdownLatch.await();
            } catch (Throwable e) {
                System.exit(1);
            }
        }
        System.exit(0);
    }

    static Map<String, String> propertiesToMap(final Properties properties) {
        final Map<String, String> configs = new HashMap<>();
        properties.forEach((key, value) -> configs.put((String) key, (String) value));
        return configs;
    }


    static <T extends SpecificRecord> SpecificAvroSerde<T> getSpecificAvroSerde(final Map<String, String> serdeConfig) {
        final SpecificAvroSerde<T> specificAvroSerde = new SpecificAvroSerde<>();
        specificAvroSerde.configure(serdeConfig, false);
        return specificAvroSerde;
    }
}
