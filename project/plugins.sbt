logLevel := Level.Warn

addDependencyTreePlugin

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.1.0")

addSbtPlugin("com.github.sbt" % "sbt-avro" % "3.4.2")

libraryDependencies += "org.apache.avro" % "avro-compiler" % "1.11.1"
