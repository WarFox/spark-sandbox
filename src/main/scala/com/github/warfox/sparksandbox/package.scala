package com.github.warfox

// This class is generated from Pageview avro schema, using sbt-avro
import sandbox.avro.Pageview

package object sparksandbox {

  // val recordRDD: Dataset[KafkaRecord] = df.as[KafkaRecord]
  // the above conversion did not work with avro data for iceberg
  // Problems:
  // * key: binary cannot be promoted to string
  // * value: binary cannot be promoted to string

  case class Pageviews(pageview: Pageview)
  case class Pagecount(pageid: String, count: Long)
  case class Output(
      key: String,
      value: String
  ) // value needs to binary or string
}
