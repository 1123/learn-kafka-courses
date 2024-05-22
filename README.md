# Kafka Streams Course

This is the code repo containing the exercises plus solutions
for the Kafka Streams course.

## Setting up properties for running the exercise code

Copy the file `src/main/resources/streams.properties.orig` to 
`src/main/resources/streams.properties` and fill in the details for the 
Kafka bootstrap servers, keystores and truststores and Schema Registry URL and 
authentication to Schema Registry. These parameters will be given to you at the 
beginning of the course. 

# Naming convention of the topics

The topic names indicate whether they are used as input or output topics for the exercises. 

Topics are prefixed by your participant name, such that the participants of this course do not interfere with each other.
The participant name is read from the property `participant.name` in the properties file. 

## Running the exercises

The first step before working with any of streams exercises will be to run `mvn clean package`. This will generate all of the
Avro classes from the schemas contained in the `/src/main/avro` directory. 
The generated classes are located under `/target/generated-sources/avro`.
Make sure this directory is marked as a _generated sources root_ folder in your IDE. 

The exercises are self-contained and can be run independently of any other exercise. 

Each streams application will print the records coming into the topology, and the records going out of the topology. 
Also, some streams applications will print to the console from different handlers. 
To that end, you should let each application run for at least 40 seconds, as some of them don't have immediate output.

Every application uses a utility class, `TopicLoader`, which will create the required topics and populate them with some sample data.

Finally, to run an exercise from the command line (assuming you are in the root directory of the repo) run one of the following commands. Remember to specify the `participant.name` property in your properties file. 

* `mvn exec:java -Dexec.mainClass=io.confluent.developer.ktable.KTableExample`
* `mvn exec:java -Dexec.mainClass=io.confluent.developer.aggregate.StreamsAggregate`
* `mvn exec:java -Dexec.mainClass=io.confluent.developer.serdes.StreamsSerdes`
* `mvn exec:java -Dexec.mainClass=io.confluent.developer.serdes.StreamsSerdesSchemaRegistry`
* `mvn exec:java -Dexec.mainClass=io.confluent.developer.processor.ProcessorApi`
* `mvn exec:java -Dexec.mainClass=io.confluent.developer.basic.BasicStreams`
* `mvn exec:java -Dexec.mainClass=io.confluent.developer.time.StreamsTimestampExtractor`
* `mvn exec:java -Dexec.mainClass=io.confluent.developer.joins.StreamsJoin`
* `mvn exec:java -Dexec.mainClass=io.confluent.developer.errors.StreamsErrorHandling`
* `mvn exec:java -Dexec.mainClass=io.confluent.developer.windows.StreamsWindows`

To run any of the TopicLoader main classes separately, use one of the following commands: 

* `mvn exec:java -Dexec.mainClass="io.confluent.developer.ktable.TopicLoader"`
* `mvn exec:java -Dexec.mainClass="io.confluent.developer.aggregate.TopicLoader"`
* `mvn exec:java -Dexec.mainClass="io.confluent.developer.serdes.TopicLoader"`
* `mvn exec:java -Dexec.mainClass="io.confluent.developer.serdes.SRTopicLoader"`
* `mvn exec:java -Dexec.mainClass="io.confluent.developer.processor.TopicLoader"`
* `mvn exec:java -Dexec.mainClass="io.confluent.developer.basic.TopicLoader"`
* `mvn exec:java -Dexec.mainClass="io.confluent.developer.time.TopicLoader"`
* `mvn exec:java -Dexec.mainClass="io.confluent.developer.joins.TopicLoader"`
* `mvn exec:java -Dexec.mainClass="io.confluent.developer.errors.TopicLoader"`
* `mvn exec:java -Dexec.mainClass="io.confluent.developer.windows.TopicLoader"`

### Exercise Descriptions

Here's a brief description of each example in this repository. 
For detailed step-by-step descriptions follow the Kafka Streams course videos.  
Note that for the purposes of facilitating the learning process, each exercise uses a utility class `TopicLoader` that will create the required topics and populate them with some sample records for the Kafka Streams application. 
As a result when you run each exercise, the first output you'll see on the console is from the `Callback` interface, and it will look similar to this:

```text
Record produced - offset - 0 timestamp - 1622133855705 
Record produced - offset - 1 timestamp - 1622133855717 
Record produced - offset - 2 timestamp - 1622133855717 
```

Each exercise is incomplete, and it's up to you to follow the instructions and hints in the comments to get each application into running shape.  
There's also a `solution` directory in each module that contains the fully completed example for you to compare with your version or to help you if you get stuck.

#### Basic Operations

It's recommended to watch the [Basic Operations lecture](https://developer.confluent.io/learn-kafka/kafka-streams/basic-operations/) and the [Hands On: Basic Operations](https://developer.confluent.io/learn-kafka/kafka-streams/hands-on-basic-operations/) videos first.

The basic operations exercise demonstrates using Kafka Streams stateless operations like `filter` and `mapValues`. 
You run the basic operations example with this command `mvn exec:java -Dexec.mainClass=io.confluent.developer.basic.BasicStreams` and your output on the console should resemble this:
```text
Incoming record - key order-key value orderNumber-1001
Outgoing record - key order-key value 1001
Incoming record - key order-key value orderNumber-5000
Outgoing record - key order-key value 5000
Incoming record - key order-key value orderNumber-999
Incoming record - key order-key value orderNumber-3330
Outgoing record - key order-key value 3330
Incoming record - key order-key value bogus-1
Incoming record - key order-key value bogus-2
Incoming record - key order-key value orderNumber-8400
Outgoing record - key order-key value 8400
```
Take note that it's expected to not have a corresponding output record for each input record due to the filters applied by the Kafka Steams application.

#### KTable 
It's recommended to watch the [KTable lecture](https://developer.confluent.io/learn-kafka/kafka-streams/ktable/) and the [Hands On: KTable](https://developer.confluent.io/learn-kafka/kafka-streams/hands-on-ktable/) videos first.

This exercise is a gentle introduction to the Kafka Streams `KTable` abstraction.  This example uses the same topology as the `basic` example, but your expected output
is different due to fact that a `KTable` is an update-stream, and records with the same key are considered updates to previous records.  The default behavior 
of a `KTable` then is to emit only the latest update per key. 

You run the `KTable`  example with this command ` mvn exec:java -Dexec.mainClass=io.confluent.developer.ktable.solution.KTableExample` and your output on the console should resemble this: 
```text
Outgoing record - key order-key value 8400
```
The sample data for this exercise has the same key, so your output for this exercise contains only one record.

NOTE: Since the default behavior for materialized `KTable`s is to emit changes on commit or when the cache is full, you'll need 
to let this application run for roughly 40 seconds to see a result.

#### Joins

It's recommended to watch the [Joins lecture](https://developer.confluent.io/learn-kafka/kafka-streams/joins/) and the [Hands On: Joins](https://developer.confluent.io/learn-kafka/kafka-streams/hands-on-joins/) videos first.

The Joins exercise creates a join between two `KStream` objects resulting in a new `KStream` which is 
further joined against a `KTable`.  You'll see the input records for the two `KStream`s , the results of the 
Stream-Stream join, and the final Stream-Table join results.  

You run the joins example with this command `mvn exec:java -Dexec.mainClass=io.confluent.developer.joins.StreamsJoin`
 and the output for the exercise should like this:
```text
Appliance stream incoming record key 10261998 value {"order_id": "remodel-1", "appliance_id": "dishwasher-1333", "user_id": "10261998", "time": 1622148573134}
Electronic stream incoming record 10261999 value {"order_id": "remodel-2", "electronic_id": "laptop-5333", "user_id": "10261999", "price": 0.0, "time": 1622148573146}
Electronic stream incoming record 10261998 value {"order_id": "remodel-1", "electronic_id": "television-2333", "user_id": "10261998", "price": 0.0, "time": 1622148573136}
Stream-Stream Join record key 10261998 value {"electronic_order_id": "remodel-1", "appliance_order_id": "remodel-1", "appliance_id": "dishwasher-1333", "user_name": "", "time": 1622148582747}
Stream-Table Join record key 10261998 value {"electronic_order_id": "remodel-1", "appliance_order_id": "remodel-1", "appliance_id": "dishwasher-1333", "user_name": "Elizabeth Jones", "time": 1622148582747}
Appliance stream incoming record key 10261999 value {"order_id": "remodel-2", "appliance_id": "stove-2333", "user_id": "10261999", "time": 1622148573134}
Stream-Stream Join record key 10261999 value {"electronic_order_id": "remodel-2", "appliance_order_id": "remodel-2", "appliance_id": "stove-2333", "user_name": "", "time": 1622148582853}
Stream-Table Join record key 10261999 value {"electronic_order_id": "remodel-2", "appliance_order_id": "remodel-2", "appliance_id": "stove-2333", "user_name": "", "time": 1622148582853}

```

#### Aggregation

It's recommended to watch the [Stateful operations](https://developer.confluent.io/learn-kafka/kafka-streams/stateful-operations/) and the [Hands On: Aggregations](https://developer.confluent.io/learn-kafka/kafka-streams/hands-on-aggregations/) videos first.

This exercise demonstrates an aggregation of a simulated stream of electronic purchase. 
To run the aggregation example use this command `mvn exec:java -Dexec.mainClass=io.confluent.developer.aggregate.StreamsAggregate` 
You'll see the incoming records on the console along with the aggregation results:

```text
Incoming record - key HDTV-2333 value {"order_id": "instore-1", "electronic_id": "HDTV-2333", "user_id": "10261998", "price": 2000.0, "time": 1622149038018}
Incoming record - key HDTV-2333 value {"order_id": "instore-1", "electronic_id": "HDTV-2333", "user_id": "1033737373", "price": 1999.23, "time": 1622149048018}
Incoming record - key HDTV-2333 value {"order_id": "instore-1", "electronic_id": "HDTV-2333", "user_id": "1026333", "price": 4500.0, "time": 1622149058018}
Incoming record - key HDTV-2333 value {"order_id": "instore-1", "electronic_id": "HDTV-2333", "user_id": "1038884844", "price": 1333.98, "time": 1622149070018}
Outgoing record - key HDTV-2333 value 9833.21
```
NOTE that you'll need to let the streams application run for ~40 seconds to see the aggregation result

#### Windowing

It's recommended to watch the [Windowing](https://developer.confluent.io/learn-kafka/kafka-streams/windowing/) and the [Hands On: Windowing](https://developer.confluent.io/learn-kafka/kafka-streams/hands-on-windowing/) videos before attempting the exercises.

This exercise uses builds on top of the aggregation exercise, but adds windowing to it.  
 
To run the windowing exercise execute this command ` mvn exec:java -Dexec.mainClass=io.confluent.developer.windows.StreamsWindows`
You'll use slightly different input records, and your output should look something like this:
```text
Incoming record - key HDTV-2333 value {"order_id": "instore-1", "electronic_id": "HDTV-2333", "user_id": "10261998", "price": 2000.0, "time": 1622152480629}
Incoming record - key HDTV-2333 value {"order_id": "instore-1", "electronic_id": "HDTV-2333", "user_id": "1033737373", "price": 1999.23, "time": 1622153380629}
Incoming record - key HDTV-2333 value {"order_id": "instore-1", "electronic_id": "HDTV-2333", "user_id": "1026333", "price": 4500.0, "time": 1622154280629}
Incoming record - key HDTV-2333 value {"order_id": "instore-1", "electronic_id": "HDTV-2333", "user_id": "1038884844", "price": 1333.98, "time": 1622155180629}
Incoming record - key HDTV-2333 value {"order_id": "instore-1", "electronic_id": "HDTV-2333", "user_id": "1038884844", "price": 1333.98, "time": 1622156260629}
Incoming record - key SUPER-WIDE-TV-2333 value {"order_id": "instore-1", "electronic_id": "SUPER-WIDE-TV-2333", "user_id": "1038884844", "price": 5333.98, "time": 1622156260629}
Incoming record - key SUPER-WIDE-TV-2333 value {"order_id": "instore-1", "electronic_id": "SUPER-WIDE-TV-2333", "user_id": "1038884844", "price": 4333.98, "time": 1622158960629}
Outgoing record - key HDTV-2333 value 2000.0
Outgoing record - key HDTV-2333 value 9167.189999999999
Outgoing record - key SUPER-WIDE-TV-2333 value 5333.98
```
Three things to note about this example:
1. The timestamps on the record are simulated to emit windowed results so what you'll see is approximated
2. This application uses the default timestamp extractor [FailOnInvalidTimestamp](https://kafka.apache.org/10/documentation/streams/developer-guide/config-streams.html#timestamp-extractor) 
3. You need to let the application run for ~40 seconds to see the windowed aggregated output

#### Time Concepts

It's recommended to watch the [Time Concepts](https://developer.confluent.io/learn-kafka/kafka-streams/time-concepts/) and the [Hands On: Time Concepts](https://developer.confluent.io/learn-kafka/kafka-streams/hands-on-time-concepts/) videos before moving on to the exercises.

The time concepts exercise uses an aggregation with windowing.  However, this example uses a custom 
`TimestampExtractor` to use timestamps embedded in the record itself (event time) to drive the behavior of Kafka Steams
application.  

To run this example execute ` mvn exec:java -Dexec.mainClass=io.confluent.developer.time.StreamsTimestampExtractor`.

Your output will include statements from the `TimestampExtractor` and it should look
something like this:
```text
Extracting time of 1622155705696 from {"order_id": "instore-1", "electronic_id": "HDTV-2333", "user_id": "10261998", "price": 2000.0, "time": 1622155705696}
Extracting time of 1622156605696 from {"order_id": "instore-1", "electronic_id": "HDTV-2333", "user_id": "1033737373", "price": 1999.23, "time": 1622156605696}
Incoming record - key HDTV-2333 value {"order_id": "instore-1", "electronic_id": "HDTV-2333", "user_id": "10261998", "price": 2000.0, "time": 1622155705696}
Extracting time of 1622157505696 from {"order_id": "instore-1", "electronic_id": "HDTV-2333", "user_id": "1026333", "price": 4500.0, "time": 1622157505696}
Incoming record - key HDTV-2333 value {"order_id": "instore-1", "electronic_id": "HDTV-2333", "user_id": "1033737373", "price": 1999.23, "time": 1622156605696}
Extracting time of 1622158405696 from {"order_id": "instore-1", "electronic_id": "HDTV-2333", "user_id": "1038884844", "price": 1333.98, "time": 1622158405696}
Incoming record - key HDTV-2333 value {"order_id": "instore-1", "electronic_id": "HDTV-2333", "user_id": "1026333", "price": 4500.0, "time": 1622157505696}
Extracting time of 1622159485696 from {"order_id": "instore-1", "electronic_id": "HDTV-2333", "user_id": "1038884844", "price": 1333.98, "time": 1622159485696}
Incoming record - key HDTV-2333 value {"order_id": "instore-1", "electronic_id": "HDTV-2333", "user_id": "1038884844", "price": 1333.98, "time": 1622158405696}
Incoming record - key HDTV-2333 value {"order_id": "instore-1", "electronic_id": "HDTV-2333", "user_id": "1038884844", "price": 1333.98, "time": 1622159485696}
Outgoing record - key HDTV-2333 value 2000.0
Outgoing record - key HDTV-2333 value 9167.189999999999
```

Two things to note about this example:
1. The timestamps on the record are simulated to emit windowed results so what you'll see is approximated
2. You need to let the application run for ~40 seconds to see the windowed aggregated output

#### Processor API

It's recommended to watch the [Processor API](https://developer.confluent.io/learn-kafka/kafka-streams/processor-api/) and [Hands On: Processor API](https://developer.confluent.io/learn-kafka/kafka-streams/hands-on-processor-api/) videos before moving on to the exercises.

This exercise covers working with the Processor API.  The application creates an aggregation but uses a punctuation every 30 seconds
(stream-time) to emit records.

You run this example with the command: ` mvn exec:java -Dexec.mainClass=io.confluent.developer.processor.ProcessorApi` and the results should look like this:
```text
Processed incoming record - key HDTV-2333 value {"order_id": "instore-1", "electronic_id": "HDTV-2333", "user_id": "10261998", "price": 2000.0, "time": 1622156159867}
Punctuation forwarded record - key HDTV-2333 value 2000.0
Processed incoming record - key HDTV-2333 value {"order_id": "instore-1", "electronic_id": "HDTV-2333", "user_id": "1033737373", "price": 1999.23, "time": 1622156194867}
Punctuation forwarded record - key HDTV-2333 value 3999.23
Processed incoming record - key HDTV-2333 value {"order_id": "instore-1", "electronic_id": "HDTV-2333", "user_id": "1026333", "price": 4500.0, "time": 1622156229867}
Punctuation forwarded record - key HDTV-2333 value 8499.23
Processed incoming record - key HDTV-2333 value {"order_id": "instore-1", "electronic_id": "HDTV-2333", "user_id": "1038884844", "price": 1333.98, "time": 1622156264867}
Punctuation forwarded record - key HDTV-2333 value 9833.21
```
Note that for this example the timestamps have been modified to advance stream-time by 30 seconds for each incoming record.
The output here does not reflect what you would see on a production system.

#### Error Handling

It's recommended to watch the [Error Handling](https://developer.confluent.io/learn-kafka/kafka-streams/error-handling/) and [Hands On: Error Handling](https://developer.confluent.io/learn-kafka/kafka-streams/hands-on-error-handling/) videos before attempting the exercises.

The error handling exercise injects a simulated transient error. The Kafka Streams `StreamsUncaughtExceptionHandler` 
examines the exception and returns a `StreamThreadExceptionResponse.REPLACE_THREAD` response that allows the application
to resume processing after the error. 

Use this command, ` mvn exec:java -Dexec.mainClass=io.confluent.developer.errors.StreamsErrorHandling`, to run the errors example.

When the application runs you'll see a stacktrace then in a few seconds the application will recover and continue running:
```text
Incoming record - key order-key value orderNumber-1001
Exception in thread "streams-error-handling-f589722e-89f3-4304-a38e-77a9b9ad5166-StreamThread-1" org.apache.kafka.streams.errors.StreamsException: Exception caught in process. taskId=0_4, processor=KSTREAM-SOURCE-0000000000, topic=streams-error-input, partition=4, offset=0, stacktrace=java.lang.IllegalStateException: Retryable transient error
...(full stacktrace not shown here for clarity)
Incoming record - key order-key value orderNumber-1001
Outgoing record - key order-key value 1001
Incoming record - key order-key value orderNumber-5000
Outgoing record - key order-key value 5000
Incoming record - key order-key value orderNumber-999
Incoming record - key order-key value orderNumber-3330
Outgoing record - key order-key value 3330
Incoming record - key order-key value bogus-1
Incoming record - key order-key value bogus-2
Incoming record - key order-key value orderNumber-8400
Outgoing record - key order-key value 8400
```

#### Testing

It's recommended to watch the [Testing](https://developer.confluent.io/learn-kafka/kafka-streams/testing/) and the [Hands On: Testing](https://developer.confluent.io/learn-kafka/kafka-streams/hands-on-testing/) videos before attempting the exercises.

To run the unit test with the `TopologyTestDriver` you can either execute `mvn test` from the root of the project
or run the `io.confluent.developer.aggregate.StreamsAggregateTest` from a test runner in your IDE.



   
   


                         
