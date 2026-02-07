--drop table  IF  EXISTS sys_key_value;
--drop table  IF  EXISTS sys_file;
--drop table  IF  EXISTS media_tag;
--drop table  IF  EXISTS media_file_tag_relation;
--drop table  IF  EXISTS media_resource_config;


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
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    code VARCHAR(255) NOT NULL,           -- 编号
    hash VARCHAR(64),                     -- sha256

    original_name VARCHAR(1024) NOT NULL, -- 文件真实名称
    display_name VARCHAR(1024),           -- 显示名称，可为空默认=original_name
    suffix VARCHAR(64),                   -- 文件后缀

    media_type VARCHAR(32),               -- VIDEO / AUDIO / IMAGE / FILE / THUMBNAIL
    mime_type VARCHAR(128),               -- MIME 类型，如 video/mp4

    storage_type VARCHAR(16),             -- LOCAL / OSS / NAS
    path VARCHAR(2048) NOT NULL,          -- 存储路径（本地绝对路径或 OSS的路径）
    thumbnail VARCHAR(2048) ,             -- 缩略图code（是本表的其他行数据的code（缩略图的索引也存储在本表））

    length BIGINT NOT NULL,               -- 文件大小（byte）
    version INT NOT NULL,                 -- 文件版本
    status INT NOT NULL DEFAULT 1,        -- 逻辑删除，1=正常，0=删除
    reference_count INT NOT NULL DEFAULT 0,  -- 引用计数
    remark VARCHAR(500),                  -- 描述
    task_status VARCHAR(32)   ,           -- 文件处理进度
    maturity_level VARCHAR(32) ,          -- 多媒体内容的分级
    duration BIGINT,                      -- 多媒体内容的时长
    author VARCHAR(64)

);

CREATE UNIQUE INDEX idx_sys_file_code ON sys_file (code);


--- 系统媒体库目录配置表
CREATE TABLE IF NOT EXISTS media_resource_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 注意 H2 2.x 的语法
    path VARCHAR(2048) NOT NULL,            -- 文件本地存放位置--本地存储的情况应该是服务器的绝对路径
    task_status VARCHAR(32)   ,             -- 文件处理进度
    remark VARCHAR(500),                    -- 描述
    total_file_length BIGINT NOT NULL DEFAULT 0, -- 文件大小（byte）
    total_file_count INT NOT NULL DEFAULT 0,  -- 计数
    total_folder_count INT NOT NULL DEFAULT 0,-- 计数
    duration BIGINT                           -- 多媒体内容的时长
);


CREATE TABLE IF NOT EXISTS media_tag(
    code VARCHAR(64) PRIMARY KEY, -- 编号 UUID
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建时间
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 更新时间
    display_name VARCHAR(255),  -- 显示名称（可为空，默认=originalName）
    remark VARCHAR(512),        -- 描述
    avatar VARCHAR(1024)        -- 多个缩略图，用英文逗号分割
);

CREATE TABLE IF NOT EXISTS media_file_tag_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_code VARCHAR(255) NOT NULL,
    tag_code VARCHAR(255) NOT NULL

);

---  观看流水表 (核心：去重逻辑来源)
CREATE TABLE IF NOT EXISTS `sys_client_view_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `view_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    client_code VARCHAR(255) NOT NULL,
    file_code VARCHAR(255) NOT NULL
);

--- 流水表创建联合索引
CREATE INDEX `idx_sys_client_view_log_lookup` ON `sys_client_view_log`(`client_code`, `file_code`);


CREATE TABLE IF NOT EXISTS sys_mq_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    topic VARCHAR(64) NOT NULL,
    payload LONGTEXT NOT NULL,
    status INT NOT NULL DEFAULT 0,        -- 0:待处理, 1:处理中, 2:成功, 3:失败
    worker_id VARCHAR(128),               -- 哪个实例领走了任务
    retry_count INT DEFAULT 0,
    max_retry INT DEFAULT 3,
    version INT DEFAULT 0,                -- 乐观锁版本号
    execute_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_mq_status (topic, status, execute_time)
);
