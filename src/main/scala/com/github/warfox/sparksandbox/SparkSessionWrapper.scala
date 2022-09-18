package com.github.warfox.sparksandbox

import org.apache.spark.sql.SparkSession
import org.apache.spark.SparkContext

trait SparkSessionWrapper extends Serializable {

  lazy val spark: SparkSession = SparkSession.builder().master("local")
    .appName("spark session")
    .config("fs.s3a.aws.credentials.provider", "org.apache.hadoop.fs.s3a.AnonymousAWSCredentialsProvider")
    .getOrCreate()

  lazy val sc: SparkContext = spark.sparkContext
}
