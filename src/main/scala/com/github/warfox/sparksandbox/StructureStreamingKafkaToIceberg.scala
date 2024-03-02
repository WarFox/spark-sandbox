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


// https://iceberg.apache.org/docs/latest/spark-structured-streaming/

object StructuredStreamingKafkaToIceberg extends SparkSessionWrapper {

  def readFromKafka(spark: SparkSession, inputTopic: String) = {
    spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", Config.bootstrapServers)
      .option("groupIdPrefix", s"spark-streaming-${inputTopic}")
      .option("subscribe", inputTopic)
      .option("startingOffsets", "earliest") // From starting
      .load()
  }

  def writeToIceberg[T](ds: Dataset[T], basePath:String, catalogTable: String) = {
    println("******** Writing to Iceberg ******")
    ds.writeStream
      .format("iceberg")
      .trigger(Trigger.ProcessingTime(1, TimeUnit.MINUTES))
      // .option("path", s"${basePath}/${catalogTable}")
      .option("path", s"${catalogTable}")
      .option("checkpointLocation", Config.checkpointLocation)
      .outputMode("append")
      .start()
  }

  def main(args: Array[String]): Unit = {

    import spark.implicits._

    val inputTopic::basePath::rest = args.toList

    println("----------------------------------------------------------------------------")
    println(s"inputTopic=${inputTopic} basePath=${basePath}  rest=${rest}")
    println("-----------------------------------------------------------------------------")

    val ssc = new StreamingContext(sc, Seconds(1))

    val df = readFromKafka(spark, inputTopic)

    println("df schema")
    df.printSchema()

    println(s"df isStreaming=${df.isStreaming}.") // Returns True for DataFrames that have streaming sources

    // val value = getValue(df)
    //   .select($"partition", $"offset", deserialize_avro($"value").as("data"))

    val recordRDD: Dataset[KafkaRecord[Array[Byte], Array[Byte]]] = df.as[KafkaRecord[Array[Byte], Array[Byte]]]
    // the above conversion did not work with avro data for iceberg
    // Problems:
    // * key: binary cannot be promoted to string
    // * value: binary cannot be promoted to string

    val castToString = recordRDD
      .withColumn("key", col("key").cast(StringType))
      .withColumn("value", col("value").cast(StringType))
    // The above conversion worked, but the avro data in value is not looking good

    // schema from class generated using "sbt avroGenerate"
    val jsonFormatSchema = new Pageview().getSchema().toString
    println(s"Schema is ${jsonFormatSchema}")

    // we read the default dynamic frame
    val data = df
      .withColumn("key", col("key").cast(StringType))
      .withColumn("value", from_avro(col("value"), jsonFormatSchema))
      .withColumn("value", to_json(col("value")))

    val tableName = inputTopic.replaceAll("-", "_")
    createIcebergTable(spark, "kafka")
    val query = writeToIceberg(data, basePath, s"local.db.kafka")

    query.awaitTermination()
  }

  def createIcebergTable(spark: SparkSession, tableName: String) = {
    val ddl = Source.fromResource(s"${tableName}-iceberg.ddl").mkString
    spark.sql(ddl)
  }

  def dropIcebergTable(spark: SparkSession, tableName: String) = {
    val sql = Source.fromResource(s"${tableName}-iceberg-drop.sql").mkString
    spark.sql(sql)
  }

}
