# spark-sandbox

Sandbox project for playing around with Spark

## How to contribute

PRs welcome!

TODO: Transactional topic with no compaction - consecutive offsets
TODO: Transactional topic with compaction - non consecutive offsets

## Build

## Kafka Avro data

Structured Streaming in Confluent Platform provides =from_avro= function that
accepts Confluent Schema registry url as a parameter and handles Confluent Avro
format (wire format) automatically

https://docs.databricks.com/structured-streaming/avro-dataframe.html

## Submit

```
spark-submit --master localhost[*]
  --jars  target
  --class <main-class> \
  <application-jar> \
  [application-arguments]
```

```sh
sbt package && \
spark-submit --packages \
org.apache.spark:spark-sql_2.12:3.3.1,\
org.apache.spark:spark-streaming_2.12:3.3.1,\
org.apache.spark:spark-sql-kafka-0-10_2.12:3.3.1 \
--class com.github.warfox.sparksandbox.StructuredStreamingKafkaExample \
--name StructuredStreamingKafkaExample target/scala-2.12/spark-sandbox_2.12-0.0.1.jar
```

```sh
sbt package and
(spark-submit --packages org.apache.spark:spark-sql_2.12:3.3.1,
org.apache.spark:spark-streaming_2.12:3.3.1,
org.apache.spark:spark-sql-kafka-0-10_2.12:3.3.1
--class com.github.warfox.sparksandbox.StructuredStreamingKafkaExample
--name StructuredStreamingKafkaExample target/scala-2.12/spark-sandbox_2.12-0.0.1.jar)
```

org.apache.hadoop:hadoop-common:3.3.4,\
org.apache.hadoop:hadoop-aws:3.3.4,
com.fasterxml.jackson.core:jackson-databind:2.12.7 \
--conf spark.jars.ivySettings=./ivysettings.xml \

## References  ##

- https://docs.aws.amazon.com/glue/latest/dg/aws-glue-programming-etl-libraries.html
- https://spark.apache.org/docs/latest/quick-start.html
- https://bzhangusc.wordpress.com/2015/11/20/use-sbt-console-as-spark-shell/
- https://github.com/MrPowers/spark-examples/blob/master/src/main/scala/com/github/mrpowers/spark/examples/SparkSummit.scala
- https://sparkbyexamples.com/spark/different-ways-to-create-a-spark-dataframe/

export SBT_OPTS="-XX:+CMSClassUnloadingEnabled -Xmx2G -Xms1G"


* How to set the batch size? Time?

* Using spark with Schema Registry
https://github.com/xebia-france/spark-structured-streaming-blog/blob/master/src/main/scala/AvroConsumer.scala

https://datachef.co/blog/deserialzing-confluent-avro-record-kafka-spark/

https://www.dremio.com/blog/streaming-data-into-apache-iceberg-tables-using-aws-kinesis-and-aws-glue/
