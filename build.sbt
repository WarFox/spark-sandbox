name := "spark-sandbox"

version := "0.0.1"

scalaVersion := "2.13.6"

libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.2.1" % "provided"
libraryDependencies += "org.apache.hadoop" % "hadoop-aws" % "3.3.3"
libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "3.3.3"

libraryDependencies += "com.github.mrpowers" %% "spark-daria" % "1.2.3"

libraryDependencies += "com.github.mrpowers" %% "spark-fast-tests" % "1.1.0" % "test"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.12" % "test"

// jackson-module-scala is required for jackson-databind
// Fixes error: Scala module 2.12.3 requires Jackson Databind version >= 2.12.0 and < 2.13
libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.3"

// test suite settings
fork in Test := true
javaOptions ++= Seq("-Xms512M", "-Xmx2048M", "-XX:MaxPermSize=2048M", "-XX:+CMSClassUnloadingEnabled")
// Show runtime of tests
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oD")

// JAR file settings

// don't include Scala in the JAR file
//assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

// Add the JAR file naming conventions described here: https://github.com/MrPowers/spark-style-guide#jar-files
// You can add the JAR file naming conventions by running the shell script

initialCommands in console := s"""
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
