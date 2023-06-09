#!/usr/bin/env bash

set -ex

kafka-topics --bootstrap-server localhost:9092 --delete --topic pageviews
kafka-topics --bootstrap-server localhost:9092 --delete --topic pageviews-count
kafka-topics --bootstrap-server localhost:9092 --delete --topic pageviews-processed
kafka-topics --bootstrap-server localhost:9092 --delete --topic pageviews-compacted
