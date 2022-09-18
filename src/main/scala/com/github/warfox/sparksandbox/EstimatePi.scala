package com.github.warfox.sparksandbox

/*
https://spark.apache.org/examples.html

Spark can also be used for compute-intensive tasks. This code
estimates π by "throwing darts" at a circle. We pick random points in
the unit square ((0, 0) to (1,1)) and see how many fall in the unit
circle. The fraction should be π / 4, so we use this to get our
estimate.

 */

object EstimatePi extends SparkSessionWrapper {

  def main(args: Array[String]) = {

    val numSamples = Int.MaxValue

    val count = sc.parallelize(1 to numSamples).filter { _ =>
        val x = math.random
        val y = math.random
        x * x + y * y < 1
      }.count()

    val pi = 4.0 * count / numSamples

    println(s"Pi is roughly $pi")
  }

}
