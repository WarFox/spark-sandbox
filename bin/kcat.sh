#!/usr/bin/env bash

if ! kcat -v <the_command> &> /dev/null
then
    echo "kcat could not be found. "
    brew install kcat
fi

set -ex

# example producer and consumer
echo "one:one" | kcat -P -b localhost -t test -K:
echo "two:two" | kcat -P -b localhost -t test -K:
echo "three:three" | kcat -P  -b localhost -t test -K:

# # tombstone needs -Z
echo "three:" | kcat -P -b localhost -t test -Z -K:
echo "four:four" | kcat -P  -b localhost -t test -K:
echo "three:3" | kcat -P -b localhost -t test -K:

## example producer and consumer

kcat -C -b localhost -t test -Z -f '\nKey (%K bytes): %k\t\nValue (%S bytes): %s\nTimestamp: %T\tPartition: %p\tOffset: %o\n--\n'

# show key, value, timestamp and partition
kcat -C -b localhost -t pageviews -Z -s value=avro -r http://localhost:8081 -f 'Key: %k,Value: %s,Timestamp: %T,Partition: %p,Offset: %o\n'


# show key, value, timestamp, partition and offset with bytes
kcat -C -b localhost -t pageviews -Z -s value=avro -r http://localhost:8081 -f 'Key (%K bytes): %k,Value (%S bytes): %s,Timestamp: %T,Partition: %p,Offset: %o\n'
