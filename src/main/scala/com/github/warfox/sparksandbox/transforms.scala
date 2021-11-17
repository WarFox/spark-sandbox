package com.github.warfox.sparksandbox

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._

object transforms {

  def happyData()(df: DataFrame): DataFrame = {
    df.withColumn("happy", lit("data is fun"))
  }

}
