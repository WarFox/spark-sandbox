#!/usr/bin/env bash

set -eux

/usr/local/bin/confluent local services connect connector load datagen-pageviews --config config/connector_datagen-pageviews.config
