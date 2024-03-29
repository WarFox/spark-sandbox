package com.github.warfox.sparksandbox

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.serializers.AbstractKafkaAvroDeserializer

import org.apache.avro.Schema
import org.apache.avro.SchemaBuilder
import org.apache.avro.generic.GenericRecord

import org.apache.hudi.DataSourceReadOptions._
import org.apache.hudi.DataSourceWriteOptions._
import org.apache.hudi.QuickstartUtils._
import org.apache.hudi.common.model.HoodieRecord
import org.apache.hudi.common.table.HoodieTableConfig
import org.apache.hudi.config.HoodieWriteConfig
import org.apache.hudi.config.HoodieWriteConfig._

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.SaveMode._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.avro.SchemaConverters
import org.apache.spark.sql.avro.functions._
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types.StringType
import org.apache.spark.streaming._

import sandbox.avro.Pageview

import java.util.UUID
import java.util.concurrent.TimeUnit
import scala.collection.JavaConversions._
import scala.io.Source

// https://hudi.apache.org/docs/latest/quick-start-guide

object StructuredStreamingKafkaToHudi extends StreamingSessionWrapper {

  private val checkpointLocation = "/tmp/temporary-" + UUID.randomUUID.toString

  def readFromKafka(spark: SparkSession, inputTopic: String) = {
    spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", Config.bootstrapServers)
      .option("groupIdPrefix", s"spark-streaming-${inputTopic}")
      .option("subscribe", inputTopic)
      .option("startingOffsets", "earliest") // From starting
      .load()
  }


  def writeToHudi[T](ds: Dataset[T], basePath: String, tableName: String) = {
    println("******** Writing to Hudi ********")
    ds.writeStream
      .format("hudi")
      .options(getQuickstartWriteConfigs)
      .option(PRECOMBINE_FIELD_OPT_KEY, "timestamp")
      .option(RECORDKEY_FIELD_OPT_KEY, "timestamp")
    // .option(PARTITIONPATH_FIELD_OPT_KEY, "")
      .option(HoodieWriteConfig.TABLE_NAME, tableName)
      .option("hoodie.merge.allow.duplicate.on.inserts",true)
      .option("path", s"${basePath}/${tableName}")
      .option("checkpointLocation", checkpointLocation)
      .outputMode("append")
      .start()
  }


  def main(args: Array[String]): Unit = {
    import spark.implicits._

    val inputTopic::basePath::rest = args.toList

    println("----------------------------------------------------------------------------")
    println(s"inputTopic=${inputTopic} basePath=${basePath} rest=${rest}")
    println("-----------------------------------------------------------------------------")

    val df = readFromKafka(spark, inputTopic)

    println("df schema")
    df.printSchema()

    println(s"df isStreaming=${df.isStreaming}.") // Returns True for DataFrames that have streaming sources

    // schema from class generated using "sbt avroGenerate"
    val jsonFormatSchema = new Pageview().getSchema().toString
    println(s"Schema is ${jsonFormatSchema}")

    val tableName = inputTopic.replaceAll("-", "_")
    // we read the default dynamic frame
    val data = df
      .withColumn("key", col("key").cast(StringType))
      .withColumn("value", from_avro(col("value"), jsonFormatSchema))
      .withColumn("value", to_json(col("value")))

    val query = writeToHudi(data, basePath, tableName)
    query.awaitTermination()
  }

}
