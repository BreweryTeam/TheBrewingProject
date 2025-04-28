DROP TABLE version;

CREATE TABLE version
(
    version INTEGER DEFAULT -1,
    singleton_value DEFAULT 0,
    PRIMARY KEY (singleton_value)
);