package com.github.warfox.sparksandbox

import scala.io.Source


import org.apache.hudi.QuickstartUtils._
import scala.collection.JavaConversions._
import org.apache.spark.sql.SaveMode.Append
import org.apache.hudi.DataSourceReadOptions._
import org.apache.hudi.DataSourceWriteOptions._
import org.apache.hudi.config.HoodieWriteConfig._
import org.apache.hudi.common.model.HoodieRecord
import org.apache.hudi.common.table.HoodieTableConfig

import org.apache.spark.streaming._
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.DataFrame
import org.apache.avro.SchemaBuilder

import io.confluent.kafka.schemaregistry.client.{
  CachedSchemaRegistryClient,
  SchemaRegistryClient
}
import io.confluent.kafka.serializers.AbstractKafkaAvroDeserializer
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.spark.sql.avro.SchemaConverters

import org.apache.spark.sql.functions._
import org.apache.spark.sql.avro.functions._
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.hudi.config.HoodieWriteConfig


// This class is generated from Pageview avro schema, using sbt-avro
import sandbox.avro.Pageview

// https://hudi.apache.org/docs/0.12.2/quick-start-guide

object BatchKafkaToHudi extends SparkSessionWrapper {

  case class Pageviews(pageview: Pageview)
  case class Pagecount(pageid: String, count: Long)
  case class Output(key: String, value: String) // value needs to binary or string

  def readFromKafka(inputTopic: String) = {
    spark.read
      .format("kafka")
      .option("kafka.bootstrap.servers", Config.bootstrapServers)
      .option("groupIdPrefix", s"spark-streaming-${inputTopic}")
      .option("subscribe", inputTopic)
      .option("startingOffsets", "earliest") // From starting
      // .option("hoodie.datasource.write.recordkey.field", "viewtime")
      // .option("hoodie.datasource.write.partitionpath.field", "pageid")
      .load()
  }

  def writeToHudi[T](ds: Dataset[T], basePath: String, tableName: String) = {
    ds.write
      .format("hudi")
      .options(getQuickstartWriteConfigs)
      .option(PRECOMBINE_FIELD_OPT_KEY, "timestamp")
      .option(RECORDKEY_FIELD_OPT_KEY, "timestamp")
      .option(OPERATION.key(), "bulk_insert") // upsert, insert or bulk_insert
      // .option(PARTITIONPATH_FIELD_OPT_KEY, "")
      .option(HoodieWriteConfig.TABLE_NAME, tableName)
      .option("hoodie.merge.allow.duplicate.on.inserts",true)
      .option("checkpointLocation", Config.checkpointLocation)
      .mode(Append)
      .save(s"${basePath}/${tableName}")
  }

  def writeToConsole[T](ds: Dataset[T]) = {
    ds.writeStream
      .format("console")
      .outputMode("append") // apppend doesn't work for aggregations
      // .outputMode("complete")
      .start()
  }

  def main(args: Array[String]): Unit = {

    import spark.implicits._

    val inputTopic::basePath::output::rest = args.toList

    println("--------")
    println(s"inputTopic=${inputTopic} basePath=${basePath} rest=${rest} output=${output}")
    println("--------")

    println(getQuickstartWriteConfigs())

    val ssc = new StreamingContext(sc, Seconds(1))

    val df = readFromKafka(inputTopic)

    println("df schema")
    df.printSchema()

    println(s"df isStreaming=${df.isStreaming}.") // Returns True for DataFrames that have streaming sources

    // val value = getValue(df)
    //   .select($"partition", $"offset", deserialize_avro($"value").as("data"))

    val recordRDD: Dataset[KafkaRecord[String, String]] = df.as[KafkaRecord[String, String]]

    val data = recordRDD


    val tableName = inputTopic.replaceAll("-", "_")
    val query = output match {
      case "console" => writeToConsole(data)
      case  _ => writeToHudi(data, basePath, tableName)
    }
  }

}

// checkpointLocation must be specified either through option("checkpointLocation", ...) or SparkSession.conf.set("spark.sql.streaming.checkpointLocation", ...))
