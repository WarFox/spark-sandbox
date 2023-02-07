package com.github.warfox.sparksandbox

import org.apache.spark.streaming._

object StreamingNetworkWordCount extends SparkSessionWrapper {

  // https://spark.apache.org/docs/latest/streaming-programming-guide.html

  // Spark Streaming is a legacy project, we should be using Structured Streaming instead

  def main(args: Array[String]) = {

    // Run netcat as small data server
    // nc -lk 9999

    // Initiate StreamingContext from SparkContext
    val ssc = new StreamingContext(sc, Seconds(1))

    // Create a DStream that will connect to hostname:port, like localhost:9999
    val lines = ssc.socketTextStream("localhost", 9999)

    // Split each line into wordss
    val words = lines.flatMap(_.split(" "))

    // Count each word in each batchh
    val pairs = words.map(word => (word, 1))
    val wordCounts = pairs.reduceByKey(_ + _)

    wordCounts.print()

    ssc.start() // Start the computation
    ssc.awaitTermination() // Wait for the computation to terminate

  }
}
