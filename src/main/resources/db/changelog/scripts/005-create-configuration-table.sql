-- liquibase formatted sql

-- changeset snackoverflow:1714849200-1
CREATE TABLE snackoverflow.configuration
(
    config_key   VARCHAR(255) NOT NULL,
    config_value VARCHAR(255) NOT NULL,
    CONSTRAINT pk_configuration PRIMARY KEY (config_key)
);

-- changeset snackoverflow:1714849200-2
INSERT INTO snackoverflow.configuration (config_key, config_value)
VALUES ('minio.max.file.size.bytes', '5242880'),
       ('scheduler.auto.cancel.fixed.delay.ms', '60000');

