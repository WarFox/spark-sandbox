#!/usr/bin/env bash

mkdir -p /tmp/spark-events

output=console

while getopts "t:o:" arg; do
    case $arg in
        o) output=$OPTARG;;
        t) topic=$OPTARG;;
    esac
done

export output=$output
export topic=$topic
export base_path=/tmp/warehouse/spark/$output/

echo "Topic: ${topic}"
echo "Output: ${output}"
echo "Base Path: ${base_path}"

echo "Spark Structured Streaming from Kafka topic $topic to $output"

LOG4J_SETTING="-Dlog4j2.configurationFile=file:src/main/resources/log4j2.properties"
DEBUG="-Dlog4j2.debug=true"

set -eux

/usr/local/bin/envsubst '${output},${base_path}' < src/main/resources/spark.properties > "/tmp/spark.properties"

PACKAGES=(
    org.apache.spark:spark-avro_2.12:3.3.1 #  org.apache.spark.sql.avro.SchemaConverter
    org.apache.spark:spark-sql-kafka-0-10_2.12:3.3.1
    org.apache.hudi:hudi-spark3.3-bundle_2.12:0.12.2

    za.co.absa:abris_2.12:6.3.0

    # org.apache.iceberg:iceberg-spark-runtime-3.2_2.12:1.2.0
    # org.apache.kafka:kafka-clients:0.11.0.3

)

/usr/local/bin/spark-submit \
    --master local[1] \
    --num-executors=1 \
    --executor-cores=1 \
    --repositories https://packages.confluent.io/maven/ \
    --packages $(IFS=,; echo "${PACKAGES[*]}") \
    --conf "spark.driver.extraJavaOptions=${LOG4J_SETTING}" \
    --conf "spark.executor.extraJavaOptions=${LOG4J_SETTING}" \
    --properties-file /tmp/spark.properties \
    --class "com.github.warfox.sparksandbox.StructuredStreamingKafkaTo${output}" \
    --name "StructuredStreamingKafkaTo${output}" \
    target/scala-2.12/spark-sandbox-assembly-0.0.1.jar $topic $base_path
