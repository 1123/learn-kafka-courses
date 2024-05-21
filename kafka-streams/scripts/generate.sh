#!/usr/local/bin/bash

for pair in joins:StreamsJoin ktable:KTableExample \
  processor:ProcessorApi serdes:StreamsSerdes \
  time:StreamsTimestampExtractor windows:StreamsWindows; do
  echo $pair
  PACKAGE=$(echo $pair | cut -d ":" -f 1)
  CLASS=$(echo $pair | cut -d ":" -f 2)
  echo "PACKAGE: $PACKAGE"
  echo "CLASS: $CLASS"
  TOPIC_LOADER_SCRIPT=runStreams"${PACKAGE^}"TopicLoader.sh
  MAIN_SCRIPT=runStreams"${PACKAGE^}"Solution.sh
  echo $TOPIC_LOADER_SCRIPT
  echo $MAIN_SCRIPT
  echo "cd ..; mvn exec:java -Dexec.mainClass=\"io.confluent.developer.$PACKAGE.TopicLoader\"" > $TOPIC_LOADER_SCRIPT
  chmod +x $TOPIC_LOADER_SCRIPT
  echo "cd ..; mvn exec:java -Dexec.mainClass=\"io.confluent.developer.$PACKAGE.solution.$CLASS\"" > $MAIN_SCRIPT
  chmod +x $MAIN_SCRIPT
done
