package io.confluent.developer.serdes.solution;

import io.confluent.developer.StreamsUtils;
import io.confluent.developer.serdes.TopicLoader;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import java.io.IOException;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import static io.confluent.developer.StreamsUtils.prefixedTopicName;

public class StreamsSerdes {

    public static void main(String[] args) throws IOException {
        final Properties streamsProps = StreamsUtils.loadProperties();
        StreamsUtils.setApplicationID(streamsProps, "serdes-streams");

        StreamsBuilder builder = new StreamsBuilder();
        final String inputTopic = prefixedTopicName(streamsProps.getProperty("serdes.input.topic"));
        final String outputTopic = prefixedTopicName(streamsProps.getProperty("serdes.output.topic"));

        final String orderNumberStart = "orderNumber-";
        KStream<String, String> firstStream = builder.stream(inputTopic, Consumed.with(Serdes.String(), Serdes.String()));

        firstStream.filter((key, value) -> value.contains(orderNumberStart))
                .mapValues(value -> value.substring(value.indexOf(orderNumberStart)))
                .filter((key, value) -> Long.parseLong(value) > 1000)
                .to(outputTopic, Produced.with(Serdes.String(), Serdes.String()));

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
