#!/usr/bin/env bash

set -ex

# https://docs.confluent.io/kafka/operations-tools/partition-determination.html
# A topic partition is the unit of parallelism in Apache Kafka®.

# For both producers and brokers, writes to different partitions can be done in
# parallel. This frees up hardware resources for expensive operations such as
# compression.

# If you want to change the number of partitions or replicas of your Kafka topic,
# you can use a streaming transformation to automatically stream all of the
# messages from the original topic into a new Kafka topic that has the desired
# number of partitions or replicas.

# https://medium.com/swlh/introduction-to-topic-log-compaction-in-apache-kafka-3e4d4afd2262

kafka-topics --bootstrap-server localhost:9092 --create --topic pageviews \
             --partitions 5

kafka-topics --bootstrap-server localhost:9092 --create --topic pageviews-count \
             --partitions 3

kafka-topics --bootstrap-server localhost:9092 --create --topic pageviews-processed \
             --partitions 5

kafka-topics --bootstrap-server localhost:9092 --create --topic pageviews-compacted \
             --partitions 5 \
             --config cleanup.policy=compact \
             --config delete.retention.ms=100 \
             --config segment.ms=100 \
             --config min.cleanable.dirty.ratio=0.01
