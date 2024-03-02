#!/usr/bin/env bash

# This script is used to submit a hoodie deltastreamer job to spark
# It will download the hudi-utils jar if it is not found in the current directory

hudi_utils_version=0.12.1

hudi_utils_jar=hudi-utilities-bundle_2.12-${hudi_utils_version}.jar

hudi_utils_jar_url=https://repo1.maven.org/maven2/org/apache/hudi/hudi-utilities-bundle_2.12/${hudi_utils_version}/${hudi_utils_jar}

if [ -e ${hudi_utils_jar} ]
then
    echo "hudi-utilities found in current directory"
else
    echo "hudi-utilities not found in current directory, downloading from ${hudi_utils_jar_url}"
    wget ${hudi_utils_jar_url}
fi

set -ex

export topic=$1
table_name=$(echo $topic | sed "s/-/_/g")

echo "topic is $topic, table_name is $table_name"

target_base_path=/tmp/warehouse/hudi/$table_name
target_table=$table_name

mkdir -p /tmp/spark-events

envsubst '${topic}' < src/main/resources/hoodie-conf.properties > "/tmp/hoodie-conf-${topic}.properties"

PACKAGES=(
    org.apache.kafka:kafka-clients:3.4.0
    # io.confluent:kafka-schema-registry-client:7.3.1 # confluent schema registry client
)

# --repositories https://packages.confluent.io/maven \
# --packages "${PACKAGES[*]}" \


LOG4J_SETTING="-Dlog4j2.configurationFile=file:src/main/resources/log4j2.properties"
DEBUG="-Dlog4j2.debug=true"

spark-submit \
  --master local[*] \
  --num-executors=1 \
  --executor-cores=1 \
  --class org.apache.hudi.utilities.deltastreamer.HoodieDeltaStreamer \
  --conf "spark.driver.extraJavaOptions=${LOG4J_SETTING}" \
  --conf "spark.executor.extraJavaOptions=${LOG4J_SETTING}" \
  --properties-file ~/Sandbox/spark-sandbox/src/main/resources/spark.properties \
 ~/Sandbox/spark-sandbox/${hudi_utils_jar} \
 --op INSERT \
 --props /tmp/hoodie-conf-${topic}.properties \
 --schemaprovider-class org.apache.hudi.utilities.schema.SchemaRegistryProvider \
 --source-class org.apache.hudi.utilities.sources.AvroKafkaSource \
 --source-ordering-field viewtime  \
 --table-type COPY_ON_WRITE \
 --target-base-path file://${target_base_path} \
 --target-table $target_table

# Other options
# --schemaprovider-class org.apache.hudi.utilities.schema.FilebasedSchemaProvider
# --source-class org.apache.hudi.utilities.sources.JsonKafkaSource \
