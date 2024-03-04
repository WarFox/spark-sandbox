CREATE TABLE IF NOT EXIST local.db.kafka (
    key string,
    value string,
    topic string,
    partition int,
    offset bigint,
    timestamp timestamp,
    timestampType int)
USING iceberg
PARTITIONED BY (partition);
