CREATE TABLE IF NOT EXISTS nd_user
(
    id            BIGINT      NOT NULL COMMENT 'Snowflake ID',
    username      VARCHAR(64) NOT NULL COMMENT 'Unique login name',
    password      VARCHAR(128) NOT NULL COMMENT 'BCrypt hash',
    email         VARCHAR(128)          COMMENT 'Optional email',
    total_storage BIGINT      NOT NULL DEFAULT 10737418240 COMMENT '10 GB in bytes',
    used_storage  BIGINT      NOT NULL DEFAULT 0,
    status        TINYINT     NOT NULL DEFAULT 1 COMMENT '1=active, 0=disabled',
    create_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = 'Users';
