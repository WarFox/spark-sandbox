CREATE TABLE IF NOT EXIST local.db.pageviews (
    viewtime bigint,
    userid string,
    pageid string,
    ts timestamp)
USING iceberg
PARTITIONED BY (pageid);
