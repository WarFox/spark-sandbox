name := "spark-sandbox"

version := "0.0.1"

scalaVersion := "2.13.6"

val sparkVersion = "3.3.1"
val hadoopVersion = "3.3.4"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql" % sparkVersion, "provided",
  "org.apache.spark" %% "spark-streaming" % sparkVersion, "provided",
  "org.apache.hadoop" % "hadoop-aws" % hadoopVersion,
  "org.apache.hadoop" % "hadoop-common" % hadoopVersion,
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
  "-Xms512M",
  "-Xmx2048M",
  "-XX:MaxPermSize=2048M",
  "-XX:+CMSClassUnloadingEnabled"
)
// Show runtime of tests
Test / testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oD")

// JAR file settings

// don't include Scala in the JAR file
// assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

// Add the JAR file naming conventions described here: https://github.com/MrPowers/spark-style-guide#jar-files
// You can add the JAR file naming conventions by running the shell script

console / initialCommands := s"""
import org.apache.spark.sql.SparkSession

val spark = SparkSession.builder()
  .master("local[*]")
  .appName("shell")
  .config("fs.s3a.aws.credentials.provider", "org.apache.hadoop.fs.s3a.AnonymousAWSCredentialsProvider")
  .getOrCreate()

// use default provider credential chain "com.amazonaws.auth.DefaultAWSCredentialsProviderChain")

val sc = spark.sparkContext
val sqlContext = spark.sqlContext
"""
