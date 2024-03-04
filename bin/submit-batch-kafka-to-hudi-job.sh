#!/usr/bin/env bash
mkdir -p /tmp/spark-events

output=console

usage() {                                 # Function: Print a help message.
    echo "Usage: $0 [ -t topic ] [ -o console/hudi ]" 1>&2
}
exit_abnormal() {                         # Function: Exit with error.
    usage
    exit 1
}

while getopts ":t:o:" arg; do
    case $arg in
        o) output=$OPTARG
           ;;
        t) topic=$OPTARG
           ;;
        *) exit 1
           ;;
    esac
done

echo "Topic is ${topic}, Output to ${output}"

if [ -z "$topic" ] || [ -z "$output" ]; then
    exit_abnormal
fi

base_path=/tmp/warehouse/spark-batch/

echo "Spark Batch Kafka To Hudi for $topic"

LOG4J_SETTING="-Dlog4j2.configurationFile=file:src/main/resources/log4j2.properties"
DEBUG="-Dlog4j2.debug=true"

set -ex

PACKAGES=(
    org.apache.spark:spark-avro_2.12:3.3.1 #  org.apache.spark.sql.avro.SchemaConverter
    org.apache.spark:spark-sql-kafka-0-10_2.12:3.3.1
    org.apache.hudi:hudi-spark3.3-bundle_2.12:0.12.2
    # org.apache.kafka:kafka-clients:0.11.0.3
)

IFS=","

spark-submit \
    --master local[1] \
    --num-executors=1 \
    --executor-cores=1 \
    --packages "${PACKAGES[*]}" \
    --conf "spark.driver.extraJavaOptions=${LOG4J_SETTING}" \
    --conf "spark.executor.extraJavaOptions=${LOG4J_SETTING}" \
    --properties-file ~/Sandbox/spark-sandbox/src/main/resources/spark.properties \
    --class com.github.warfox.sparksandbox.BatchKafkaToHudi \
    --name BatchKafkaToHudi \
    target/scala-2.12/spark-sandbox-assembly-0.0.1.jar $topic $base_path $output
