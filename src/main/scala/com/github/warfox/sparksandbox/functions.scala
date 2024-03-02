package com.github.warfox.sparksandbox

import org.apache.spark.sql.Column
import org.apache.spark.sql.functions._

import org.apache.spark.sql.expressions.UserDefinedFunction

import org.apache.avro.specific.SpecificDatumReader
import java.nio.ByteBuffer

import io.confluent.kafka.schemaregistry.client.{
  CachedSchemaRegistryClient,
  SchemaRegistryClient
}
import org.apache.kafka.common.errors.SerializationException
import org.apache.avro.io.DecoderFactory

import sandbox.avro.Pageview
import io.confluent.kafka.serializers.AbstractKafkaAvroDeserializer
import org.apache.avro.generic.GenericRecord
import org.apache.avro.generic.GenericDatumReader

class AvroDeserializer extends AbstractKafkaAvroDeserializer {
  def this(client: SchemaRegistryClient) {
    this()
    this.schemaRegistry = client
  }
  override def deserialize(bytes: Array[Byte]): String = {
    val genericRecord = super.deserialize(bytes).asInstanceOf[GenericRecord]
    if (genericRecord == null) {
      ""
    } else {
      genericRecord.toString
    }
  }
}

object functions {

  def isEven(col: Column): Column = {
    col % 2 === lit(0)
  }

  // https://datachef.co/blog/deserialzing-confluent-avro-record-kafka-spark//
  def deserializeFromConfluentAvro(bytes: Array[Byte]): Pageview = {
    val schemaRegistryUrl = Config.schemaRegistryUrl;
    val schemaRegistry = new CachedSchemaRegistryClient(schemaRegistryUrl, 128)

    val buffer: ByteBuffer = ByteBuffer.wrap(bytes)

    // The first byte is magic byte
    if (buffer.get != 0)
      throw new SerializationException(
        "Unknown magic byte!. Expected 0 for Confluent bytes"
      )

    // The next 2 bytes are schema id
    val writeSchemaId = buffer.getInt()
    val writerSchema = schemaRegistry.getByID(writeSchemaId)

    // we want to deserialize with the last schema
    val subject = "pageview" + "-value"
    val readerSchemaId = schemaRegistry.getLatestSchemaMetadata(subject).getId
    val readerSchema = schemaRegistry.getByID(readerSchemaId)

    // this logic is quite specific to Confluent Avro format that uses schema registry for compresion
    val length = buffer.limit() - 1 - 4
    val start = buffer.position() + buffer.arrayOffset()
    val decoder = DecoderFactory.get().binaryDecoder(buffer.array(), start, length, null)

    // work with GenericRecord
    // val pageViewSchema = Pageview.getClassSchema().toString()
    // val datumReader = new GenericDatumReader[GenericRecord](pageViewSchema)

    // SpecificDatumReader needs a type
    val datumReader = new SpecificDatumReader[Pageview](writerSchema, readerSchema)
    datumReader.read(null, decoder)
  }

  val deserializeAvro: UserDefinedFunction = udf((bytes: Array[Byte]) => {
    deserializeFromConfluentAvro(bytes)
  })

}
