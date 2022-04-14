
CREATE TABLE IF NOT EXISTS TEMPFLY_DATA
(
    uuid                    CHAR(36)                       NOT NULL,
    player_time             DOUBLE(10, 2)    DEFAULT 0,
    logged_in_flight        BOOLEAN          DEFAULT 0,
    compat_logged_in_flight BOOLEAN          DEFAULT 0,
    damage_protection       BOOLEAN          DEFAULT 0,
    last_daily_bonus        BIGINT           DEFAULT 0,
    trail                   VARCHAR(32)      DEFAULT NULL,
    infinite                BOOLEAN          DEFAULT 0,
    bypass                  BOOLEAN          DEFAULT 0,
    speed                   DOUBLE(5, 2)     DEFAULT -999.00,

    PRIMARY KEY (uuid)
);