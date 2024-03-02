name := "spark-sandbox"

version := "0.0.1"

scalaVersion := "2.12.15"

val sparkVersion = "3.3.1"
val hadoopVersion = "3.3.4"
val confluentVersion = "7.3.1"

resolvers += "Confluent" at "https://packages.confluent.io/maven/"

libraryDependencies ++= Seq(
  /** Provided Section **/

  // Spark library with same version MUST be available in the cluster that the jobs run
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-streaming" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-avro" % sparkVersion % "provided" ,

  "org.apache.hudi" %% "hudi-spark3.3-bundle" % "0.12.1" % "provided",

  // Hadoop  libraries with same version MUST be available in the cluster that the jobs run
  "org.apache.hadoop" % "hadoop-aws" % hadoopVersion % "provided",
  "org.apache.hadoop" % "hadoop-common" % hadoopVersion % "provided",

  /** End of Provided Section - libraries in provided section is not included in assembly jar **/

  // thrird party library for using Confluent Schema Registry with Spark
  "za.co.absa" % "abris_2.12" % "6.3.0",
  "io.confluent" % "kafka-schema-registry-client" % confluentVersion excludeAll(
    ExclusionRule(organization = "com.fasterxml.jackson.module", name = "jackson-module-scala")
  ),
  "io.confluent" % "kafka-avro-serializer" % confluentVersion excludeAll(
    ExclusionRule(organization = "com.fasterxml.jackson.module", name = "jackson-module-scala")
  ),

  "com.github.mrpowers" %% "spark-daria" % "1.2.3",

  // jackson-module-scala is required for jackson-databind
  // Fixes error: Scala module 2.12.3 requires Jackson Databind version >= 2.12.0 and < 2.13
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.3"
)

// Test Dependencies
libraryDependencies ++= Seq(
  "com.github.mrpowers" %% "spark-fast-tests" % "1.1.0" % "test",
  "org.scalatest" %% "scalatest" % "3.2.12" % "test"
)

// test suite settings
Test / fork := true

javaOptions ++= Seq(
  "-Xms1G",
  "-Xmx2G",
  "-XX:+CMSClassUnloadingEnabled"
)
// The above is equilavent as export SBT_OPTS="-XX:+CMSClassUnloadingEnabled -Xmx2G -Xms1GG

// Show runtime of tests
Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oD")

// JAR file settings

ThisBuild / assemblyShadeRules := Seq(
  ShadeRule.rename(("com.fasterxml.jackson.**") -> "shadejackson.@1").inAll
)

// don't include Scala in the JAR file
// assembly / assemblyOption in := (assembly / assemblyOption).value.copy(includeScala = false)
// ThisBuild / assemblyOption := assembly / assemblyOption ~= { _.withIncludeScala(false) }
// ThisBuild / assemblyPackageScala := false

// Add the JAR file naming conventions described here: https://github.com/MrPowers/spark-style-guide#jar-files
// You can add the JAR file naming conventions by running the shell script

// When you run sbt console - spark, sc and sqlContext will be ready for you!
console / initialCommands := s"""
import org.apache.spark.sql.SparkSession

val spark = SparkSession.builder()
  .master("local[*]")
  .appName("shell")
  .config("fs.s3a.aws.credentials.provider", "org.apache.hadoop.fs.s3a.AnonymousAWSCredentialsProvider") // only needed for dealing with public S3 buckets
  .getOrCreate()

// use default provider credential chain "com.amazonaws.auth.DefaultAWSCredentialsProviderChain")

val sc = spark.sparkContext
val sqlContext = spark.sqlContext
"""
