-- ============================================================
-- nd-files tables  (schema: networkdisk_v2)
-- Run AFTER nd-auth/src/main/resources/db/init.sql
-- ============================================================

CREATE TABLE IF NOT EXISTS file
(
    id                        BIGINT       NOT NULL COMMENT 'Snowflake ID',
    filename                  VARCHAR(255) NOT NULL COMMENT 'Original file name',
    real_path                 VARCHAR(512) NOT NULL COMMENT 'MinIO object path',
    file_size                 BIGINT       NOT NULL DEFAULT 0 COMMENT 'Bytes',
    file_size_desc            VARCHAR(32)           COMMENT 'Human-readable size, e.g. 1.2 MB',
    file_suffix               VARCHAR(32)           COMMENT 'Extension without dot, e.g. pdf',
    file_preview_content_type VARCHAR(128)          COMMENT 'MIME type for browser preview',
    identifier                VARCHAR(64)  NOT NULL COMMENT 'MD5 — used for dedup (sec-upload)',
    create_user               BIGINT       NOT NULL,
    create_time               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_identifier (identifier)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = 'Physical file records — deduped by MD5';


CREATE TABLE IF NOT EXISTS user_file
(
    id          BIGINT       NOT NULL COMMENT 'Snowflake ID',
    user_id     BIGINT       NOT NULL COMMENT 'Owner (FK → nd_user.id)',
    filename    VARCHAR(255) NOT NULL COMMENT 'Display name shown to user',
    parent_id   BIGINT       NOT NULL DEFAULT 0 COMMENT '0 = root directory',
    folder_flag TINYINT      NOT NULL DEFAULT 0 COMMENT '1=folder, 0=file',
    file_type   INT                   COMMENT 'Enum: 1=video,2=music,3=image,4=doc,5=other',
    real_file_id BIGINT               COMMENT 'FK → file.id; NULL for folders',
    del_flag    TINYINT      NOT NULL DEFAULT 0 COMMENT '0=normal, 1=recycle bin',
    create_user BIGINT       NOT NULL,
    update_user BIGINT       NOT NULL,
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_parent (user_id, parent_id, del_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = 'Virtual file-system entries per user';


CREATE TABLE IF NOT EXISTS file_chunk
(
    id                  BIGINT   NOT NULL COMMENT 'Snowflake ID',
    identifier          VARCHAR(64) NOT NULL COMMENT 'MD5 of the whole file — groups chunks',
    real_path           VARCHAR(512) NOT NULL COMMENT 'MinIO path for this chunk',
    chunk_number        INT      NOT NULL COMMENT '1-based index',
    current_chunk_size  BIGINT   NOT NULL COMMENT 'Bytes in this chunk',
    total_size          BIGINT   NOT NULL COMMENT 'Total file bytes',
    create_user         BIGINT   NOT NULL,
    create_time         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expiration_time     DATETIME NOT NULL COMMENT 'Chunks not merged by this time can be purged',
    PRIMARY KEY (id),
    KEY idx_identifier (identifier)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = 'Temporary chunk records — deleted after merge';