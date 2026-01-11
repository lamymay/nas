--drop table  IF  EXISTS media_tag;
--drop table  IF  EXISTS sys_file;
--drop table  IF  EXISTS media_file_tag_relation;
--drop table  IF  EXISTS media_file_resource;


CREATE TABLE IF NOT EXISTS sys_key_value (

    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    `key` VARCHAR(255),
    `value` VARCHAR(255),
    `range` VARCHAR(255),
    remark VARCHAR(255),
    ttl BIGINT
);


CREATE TABLE IF NOT EXISTS sys_file (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    code VARCHAR(255) NOT NULL,          -- 业务编号
    hash VARCHAR(64),                     -- sha256

    original_name VARCHAR(1024) NOT NULL, -- 文件真实名称
    display_name VARCHAR(1024),           -- 显示名称，可为空默认=original_name
    suffix VARCHAR(64),                   -- 文件后缀

    media_type VARCHAR(32),               -- VIDEO / AUDIO / IMAGE / FILE / THUMBNAIL
    mime_type VARCHAR(128),               -- MIME 类型，如 video/mp4

    storage_type VARCHAR(16),             -- LOCAL / OSS / NAS
    path VARCHAR(2048) NOT NULL,          -- 存储路径（本地绝对路径或 OSS key）
    thumbnail VARCHAR(1024) ,          -- 缩略图code

    length BIGINT NOT NULL,               -- 文件大小（byte）
    version INT NOT NULL,                 -- 文件版本
    status INT NOT NULL DEFAULT 1,        -- 逻辑删除，1=正常，0=删除
    reference_count INT NOT NULL DEFAULT 0,  -- 引用计数
    remark VARCHAR(500),                  -- 描述
    task_status VARCHAR(32)   ,             -- 文件处理进度
    author VARCHAR(64) ,
    maturity_level VARCHAR(32) ,
    duration BIGINT

);


CREATE TABLE IF NOT EXISTS media_file_resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    path VARCHAR(2048) NOT NULL,          -- 文件本地存放位置--本地存储的情况应该是服务器的绝对路径
    task_status VARCHAR(32)   ,             -- 文件处理进度
    remark VARCHAR(500),                  -- 描述
    total_file_length BIGINT NOT NULL DEFAULT 0,               -- 文件大小（byte）
    total_file_count INT NOT NULL DEFAULT 0,  -- 计数
    total_folder_count INT NOT NULL DEFAULT 0,  -- 计数
    duration BIGINT

);


CREATE TABLE IF NOT EXISTS media_tag(
    code VARCHAR(64) PRIMARY KEY, -- 编号，UUID
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 更新时间
    display_name VARCHAR(255),  -- 显示名称（可为空，默认=originalName）
    avatar VARCHAR(1024),       -- 多个缩略图，用英文逗号分割
    remark VARCHAR(512)         -- 描述
);

CREATE TABLE IF NOT EXISTS media_file_tag_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_code VARCHAR(255) NOT NULL,
    tag_code VARCHAR(255) NOT NULL

);

--CREATE INDEX idx_code ON sys_file (code);
