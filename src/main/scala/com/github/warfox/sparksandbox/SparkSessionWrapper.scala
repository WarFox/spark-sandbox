package com.github.warfox.sparksandbox

import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}

trait SparkSessionWrapper extends Serializable {

  lazy val spark: SparkSession = SparkSession.builder().master("local")
    .appName("spark session")
    .config("fs.s3a.aws.credentials.provider", "org.apache.hadoop.fs.s3a.AnonymousAWSCredentialsProvider")
    .getOrCreate()

  lazy val sc: SparkContext = spark.sparkContext

}

trait StreamingSessionWrapper extends SparkSessionWrapper {
  lazy val ssc = new StreamingContext(sc, Seconds(1))
}
