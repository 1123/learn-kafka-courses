package io.confluent.developer.aggregate;

import io.confluent.developer.StreamsUtils;
import io.confluent.developer.avro.ElectronicOrder;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import static io.confluent.developer.StreamsUtils.prefixedTopicName;

public class StreamsAggregate {

    public static void main(String[] args) throws IOException {

        final Properties streamsProps = StreamsUtils.loadProperties();
        StreamsUtils.setApplicationID(streamsProps, "aggregate-streams");

        StreamsBuilder builder = new StreamsBuilder();
        final String inputTopic = prefixedTopicName(streamsProps.getProperty("aggregate.input.topic"));
        final String outputTopic = prefixedTopicName(streamsProps.getProperty("aggregate.output.topic"));
        final Map<String, Object> configMap = StreamsUtils.propertiesToMap(streamsProps);

        final SpecificAvroSerde<ElectronicOrder> electronicSerde =
                StreamsUtils.getSpecificAvroSerde(configMap);

        final KStream<String, ElectronicOrder> electronicStream =
                builder.stream(inputTopic, Consumed.with(Serdes.String(), electronicSerde))
                        .peek((key, value) -> System.out.println("Incoming record - key " + key + " value " + value));

        // Now take the electronicStream object, group by key and perform an aggregation
        // Don't forget to convert the KTable returned by the aggregate call back to a KStream using the toStream method
        electronicStream.groupByKey().aggregate(null, null);

        // To view the results of the aggregation consider
        // right after the toStream() method .peek((key, value) -> System.out.println("Outgoing record - key " +key +" value " + value))

        // convert the double value to a string, such that the data is displayed correctly in Control Center.

        // Finally write the results to an output topic
        //  .to(outputTopic, Produced.with(Serdes.String(), Serdes.String()));

        try (KafkaStreams kafkaStreams = new KafkaStreams(builder.build(), streamsProps)) {
            final CountDownLatch shutdownLatch = new CountDownLatch(1);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                kafkaStreams.close(Duration.ofSeconds(2));
                shutdownLatch.countDown();
            }));
            TopicLoader.runProducer();
            try {
                kafkaStreams.start();
                shutdownLatch.await();
            } catch (Throwable e) {
                System.exit(1);
            }
        }
        System.exit(0);
    }
}
