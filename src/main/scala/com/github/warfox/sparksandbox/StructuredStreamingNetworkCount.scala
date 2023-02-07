package com.github.warfox.sparksandbox

import org.apache.spark.streaming._
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.DataFrame

object StructuredStreamingNetworkCount extends SparkSessionWrapper {

  // https://spark.apache.org/docs/latest/structured-streaming-programming-guide.html#quick-examplee

  def main(args: Array[String]) = {

    // Run netcat as small data server
    // nc -lk 9999

    import spark.implicits._

    // Create DataFrame representing the stream of input lines from connection to localhost:9999
    val lines: DataFrame = spark.readStream
          .format("socket")
          .option("host", "localhost")
          .option("port", 9999)
          .load()

    // Split the lines into words
    val words: Dataset[String] = lines.as[String].flatMap(_.split(" "))

    // Generate running word count
    val wordCounts: DataFrame = words.groupBy("value").count()

    // Start running the query that prints the running counts to the console
    val query = wordCounts.writeStream
      .outputMode("complete")
      .format("console")
      .start()

    query.awaitTermination()

  }

}
