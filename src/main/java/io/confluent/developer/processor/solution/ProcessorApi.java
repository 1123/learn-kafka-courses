package io.confluent.developer.processor.solution;

import io.confluent.developer.StreamsUtils;
import io.confluent.developer.avro.ElectronicOrder;
import io.confluent.developer.avro.TotalPrice;
import io.confluent.developer.processor.TopicLoader;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static io.confluent.developer.StreamsUtils.*;

public class ProcessorApi {

    static class TotalPriceOrderProcessorSupplier implements ProcessorSupplier<String, ElectronicOrder, String, Double> {
        final String storeName;

        public TotalPriceOrderProcessorSupplier(String storeName) {
            this.storeName = storeName;
        }

        @Override
        public Processor<String, ElectronicOrder, String, Double> get() {
            return new Processor<>() {
                private ProcessorContext<String, Double> context;
                private KeyValueStore<String, Double> store;

                @Override
                public void init(ProcessorContext<String, Double> context) {
                    this.context = context;
                    store = context.getStateStore(storeName);
                    this.context.schedule(Duration.ofSeconds(30), PunctuationType.STREAM_TIME, this::forwardAll);
                }

                private void forwardAll(final long timestamp) {
                    try (KeyValueIterator<String, Double> iterator = store.all()) {
                        while (iterator.hasNext()) {
                            final KeyValue<String, Double> nextKV = iterator.next();
                            final Record<String, Double> totalPriceRecord = new Record<>(nextKV.key, nextKV.value, timestamp);
                            context.forward(totalPriceRecord);
                            System.out.println("Punctuation forwarded record - key " + totalPriceRecord.key() + " value " + totalPriceRecord.value());
                        }
                    }
                }

                @Override
                public void process(Record<String, ElectronicOrder> record) {
                    final String key = record.key();
                    Double currentTotal = store.get(key);
                    if (currentTotal == null) {
                        currentTotal = 0.0;
                    }
                    Double newTotal = record.value().getPrice() + currentTotal;
                    store.put(key, newTotal);
                    System.out.println("Processed incoming record - key " + key + " value " + record.value());
                }
            };
        }

        @Override
        public Set<StoreBuilder<?>> stores() {
            return Collections.singleton(totalPriceStoreBuilder);
        }
    }

    static class TotalPriceConverterSupplier implements ProcessorSupplier<String, Double, String, TotalPrice> {
        @Override
        public Processor<String, Double, String, TotalPrice> get() {
            return new Processor<>() {

                private ProcessorContext<String, TotalPrice> context;

                @Override
                public void init(ProcessorContext<String, TotalPrice> context) {
                    this.context = context;
                }

                @Override
                public void process(Record<String, Double> record) {
                    Record<String, TotalPrice> converted =
                            new Record<>(record.key(), TotalPrice.newBuilder().setPrice(record.value()).build(), record.timestamp());
                    context.forward(converted);
                }
            };
        }
    }

    final static String storeName = "total-price-store";
    static StoreBuilder<KeyValueStore<String, Double>> totalPriceStoreBuilder = Stores.keyValueStoreBuilder(
            Stores.persistentKeyValueStore(storeName),
            Serdes.String(),
            Serdes.Double());

    public static void main(String[] args) throws IOException {
        final Properties streamsProps = loadProperties();
        StreamsUtils.setApplicationID(streamsProps, "processor-api-application");

        final String inputTopic = prefixedTopicName(streamsProps.getProperty("processor.input.topic"));
        final String outputTopic = prefixedTopicName(streamsProps.getProperty("processor.output.topic"));
        final Map<String, Object> configMap = propertiesToMap(streamsProps);

        final SpecificAvroSerde<ElectronicOrder> electronicSerde = getSpecificAvroSerde(configMap);
        final Serde<String> stringSerde = Serdes.String();
        final SpecificAvroSerde<TotalPrice> totalPriceSerde = getSpecificAvroSerde(configMap);

        final Topology topology = new Topology();
        topology.addSource(
                "source-node",
                stringSerde.deserializer(),
                electronicSerde.deserializer(),
                inputTopic);

        topology.addProcessor(
                "aggregate-price",
                new TotalPriceOrderProcessorSupplier(storeName),
                "source-node");

        topology.addProcessor(
                "to-string-converter",
                new TotalPriceConverterSupplier(),
                "aggregate-price");

        topology.addSink(
                "sink-node",
                outputTopic,
                stringSerde.serializer(),
                totalPriceSerde.serializer(),
                "to-string-converter");

        try (KafkaStreams kafkaStreams = new KafkaStreams(topology, streamsProps)) {
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
