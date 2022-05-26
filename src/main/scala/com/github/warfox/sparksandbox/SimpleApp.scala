package com.github.warfox.sparksandbox

import org.apache.spark.sql.SparkSession

import org.apache.spark.sql.internal.StaticSQLConf

object SimpleApp extends  SparkSessionWrapper {

  def main(args: Array[String]) = {

    import spark.implicits._
    import spark.implicits._

    println("catalogeType: ", spark.conf.get("spark.sql.catalogImplementation"))
    println("catalogeType: ", spark.conf.get(StaticSQLConf.CATALOG_IMPLEMENTATION.key))

    val columns = Seq("language","users_count")
    val data = Seq(("Java", "20000"), ("Python", "100000"), ("Scala", "3000"))
    data.toDF(columns:_*)
    val df = data.toDF(columns:_*)

    // https://registry.opendata.aws/gnomad-data-lakehouse-ready/
    val gnomadPath = "s3a://aws-roda-hcls-datalake/gnomad/chrm/"
    val gnomadDf  =   spark.read.parquet(gnomadPath)

    // https://registry.opendata.aws/1000-genomes-data-lakehouse-ready/
    val thousandGenomes = "s3a://aws-roda-hcls-datalake/thousandgenomes_dragen/var_partby_chrom/"
    val thousandGenomesDf  =  spark.read.orc(thousandGenomes)
  }
}
