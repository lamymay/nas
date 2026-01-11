package com.arc.nas.model.domain.system.common;

import com.arc.nas.model.constants.NormalConstants;
import com.arc.util.CodeUtil;
import com.arc.util.file.FileSameCheckTool;
import com.arc.util.file.FileUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * 系统文件记录
 */
@TableName("sys_file")
public class SysFile implements Serializable {

    private static final Map<String, String> EXTENSION_TO_MIME;

    static {
        EXTENSION_TO_MIME = new HashMap<>();
        // 视频
        EXTENSION_TO_MIME.put("mp4", "video/mp4");
        EXTENSION_TO_MIME.put("mkv", "video/x-matroska");
        EXTENSION_TO_MIME.put("avi", "video/x-msvideo");
        EXTENSION_TO_MIME.put("mov", "video/quicktime");
        EXTENSION_TO_MIME.put("flv", "video/x-flv");
        EXTENSION_TO_MIME.put("wmv", "video/x-ms-wmv");
        // 音频
        EXTENSION_TO_MIME.put("mp3", "audio/mpeg");
        EXTENSION_TO_MIME.put("wav", "audio/wav");
        EXTENSION_TO_MIME.put("flac", "audio/flac");
        EXTENSION_TO_MIME.put("aac", "audio/aac");
        EXTENSION_TO_MIME.put("ogg", "audio/ogg");
        // 图片
        EXTENSION_TO_MIME.put("jpg", "image/jpeg");
        EXTENSION_TO_MIME.put("jpeg", "image/jpeg");
        EXTENSION_TO_MIME.put("png", "image/png");
        EXTENSION_TO_MIME.put("gif", "image/gif");
        EXTENSION_TO_MIME.put("bmp", "image/bmp");
        EXTENSION_TO_MIME.put("webp", "image/webp");
        // 文档
        EXTENSION_TO_MIME.put("pdf", "application/pdf");
        EXTENSION_TO_MIME.put("txt", "text/plain");
        EXTENSION_TO_MIME.put("csv", "text/csv");
        EXTENSION_TO_MIME.put("doc", "application/msword");
        EXTENSION_TO_MIME.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        EXTENSION_TO_MIME.put("xls", "application/vnd.ms-excel");
        EXTENSION_TO_MIME.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        EXTENSION_TO_MIME.put("ppt", "application/vnd.ms-powerpoint");
        EXTENSION_TO_MIME.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        // 默认
        EXTENSION_TO_MIME.put("default", "application/octet-stream");
    }

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Date createTime;// 创建时间
    private Date updateTime;// 更新时间
    private String code;//编号
    private String hash;// sha256
    private String originalName;// 文件真实存在的名称（不含路径）
    private String displayName;// 显示名称（可为空，默认=originalName）
    private String suffix;// 后缀
    private String mediaType;//  业务分类：视频/音频/图片  类型 文件还是图片 VIDEO/IMAGE/FILE/THUMBNAIL  一般来说图片是可以直接预览的,
    private String mimeType;// MIME 是标准化的 由 IANA 管理，浏览器、播放器、server 都需要 MIME。Content-Type: video/mp4
    private String storageType;// LOCAL / OSS / NAS
    private String path;// 文件本地存放位置--本地存储的情况应该是服务器的绝对路径
    private Long length;// 文件大小 单位byte
    private Integer version;// 版本信息id
    private Integer status;// 逻辑删除用的标识 0=删除的不可使用的
    private Integer referenceCount;// 引用计数 reference_count
    private String remark;// 描述
    /*
    | 状态                   | 含义          |
    | -------------------- | -------------- |
    | PENDING              | 文件刚扫描，未处理|
    | THUMBNAIL_GENERATING | 缩略图生成中     |
    | THUMBNAIL_DONE       | 缩略图生成完成   |
    | TRANSCODE_PENDING    | 转码待处理      |
    | TRANSCODING          | 转码中          |
    | TRANSCODE_DONE       | 转码完成        |
    | HASH_DONE       | HASH完成        |
    | END       | END        |
    | FAILED               | 处理失败（缩略图/转码失败） |
    */
    private String taskStatus;//标识系统对文件的处理流程
    private String thumbnail;// 多个缩略图 用英文逗号分割
    private String author;


    private String maturityLevel;//内容敏感度 “年龄分级” “PG-13”、“R”、“18+” 暴力、血腥等
    private Long duration;//持续时间

    public SysFile() {

    }


    public SysFile(Long id) {
        this.id = id;
    }

    public SysFile(String path, String code) {
        this.path = path;
        this.code = code;
    }

    /**
     * 构建可入库的数据
     *
     * @param file       MultipartFile
     * @param toDiskPath toDiskPath
     */
    public SysFile(MultipartFile file, String toDiskPath) {

        //全名
        String originalFilename = String.valueOf(file.getOriginalFilename());

        this.originalName = originalFilename;
        this.setRemark("MultipartFile");
        String suffix = "";
        int lastIndexOf = originalFilename.lastIndexOf(".");
        if (lastIndexOf != -1) {
            suffix = originalFilename.substring(lastIndexOf + 1);
        }
        this.suffix = suffix;
        this.length = file.getSize();
        this.path = toDiskPath;
//        int index = toDiskPath.lastIndexOf(File.separator);
//        this.setCode(toDiskPath.substring(index + 1, index + 45 + 1));
        Date now = new Date();
        this.createTime = now;
        this.updateTime = now;
        this.status = NormalConstants.STATUS_NOT_DELETE;
        this.version = NormalConstants.VERSION_INIT1;

    }

    @Deprecated
    public SysFile(String code, String displayName, String extension, String mediaType, String remark, String originalName, Long length, String path) {
        this(code, displayName, extension, mediaType, remark, originalName, length, path, null, null);
    }

    public SysFile(Date createTime, Date updateTime, String code, String hash, String originalName, String displayName, String suffix, String mediaType, String mimeType, String storageType, String path, Long length, Integer version, Integer status, Integer referenceCount, String remark, String taskStatus) {
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.code = code;
        this.hash = hash;
        this.originalName = originalName;
        this.displayName = displayName;
        this.suffix = suffix;
        this.mediaType = mediaType;
        this.mimeType = mimeType;
        this.storageType = storageType;
        this.path = path;
        this.length = length;
        this.version = version;
        this.status = status;
        this.referenceCount = referenceCount;
        this.remark = remark;
        this.taskStatus = taskStatus;
    }

    public SysFile(String code, String hash, String originalName, String displayName, String suffix, String mediaType, String mimeType, String storageType, String path, Long length, Integer version, Integer status, Integer referenceCount, String remark, String taskStatus) {
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.code = code;
        this.hash = hash;
        this.originalName = originalName;
        this.displayName = displayName;
        this.suffix = suffix;
        this.mediaType = mediaType;
        this.mimeType = mimeType;
        this.storageType = storageType;
        this.path = path;
        this.length = length;
        this.version = version;
        this.status = status;
        this.referenceCount = referenceCount;
        this.remark = remark;
        this.taskStatus = taskStatus;
    }

    @Deprecated
    public SysFile(String code, String displayName, String suffix, String mediaType, String remark, String originalName, Long length, String path, Integer version, Integer status) {
        Date now = new Date();
        this.createTime = now;
        this.updateTime = now;

        this.code = code;
        this.displayName = displayName;
        this.suffix = suffix;
        this.mediaType = mediaType;
        this.remark = remark;
        this.originalName = originalName;
        this.length = length;

        this.path = path;
        this.status = status == null ? NormalConstants.STATUS_NOT_DELETE : status;
        this.version = version == null ? NormalConstants.VERSION_INIT1 : version;

    }

    public static SysFile createSysFile(File file) {
        String mediaType = FileUtil.getFileType(file.getName()); // VIDEO / AUDIO / IMAGE / FILE
        return createSysFile(file, mediaType, "");
    }

    public static SysFile createSysFileSimple(File file, String taskStatus) {
        String mediaType = FileUtil.getFileType(file.getName()); // VIDEO / AUDIO / IMAGE / FILE
        String remark = "";

        Path path = file.toPath();
        BasicFileAttributes attr = null;
        if (Files.exists(path)) {
            try {
                attr = Files.readAttributes(path, BasicFileAttributes.class);
            } catch (IOException e) {
                taskStatus = "ERROR";
                remark = "Files.readAttributes error";
            }
        } else {
            taskStatus = "ERROR";
            remark = "File not exists";
        }

        long length = file.length();

        Long createTime = null;
        Long modifiedTime = null;
        if (attr != null) {
            createTime = attr.creationTime().toMillis();
            modifiedTime = attr.lastModifiedTime().toMillis();

        }
        String originalName = file.getName();
        String displayName = FileUtil.getFilenameWithoutExtension(originalName);
        String suffix = FileUtil.getExtension(originalName).toLowerCase(); // 小写统一
        String mimeType = getMimeType(suffix);  // video/mp4 / image/jpeg ...
        String storageType = "LOCAL";                    // 默认本地存储


        // 默认初始化字段
        String hash = "";          // skip计算 SHA-256
        String code = CodeUtil.createCode16();// skip
        Integer version = 1;
        Integer status = 1;                               // 1=正常
        Integer referenceCount = 0;

        if (createTime == null) {
            return new SysFile(code, hash, originalName, displayName, suffix, mediaType, mimeType, storageType, path.toString(), length, version, status, referenceCount, remark, taskStatus);
        } else {
            return new SysFile(new Date(createTime), new Date(modifiedTime), code, hash, originalName, displayName, suffix, mediaType, mimeType, storageType, path.toString(), length, version, status, referenceCount, remark, taskStatus);
        }

    }

    public static SysFile createSysFile(File file, String mediaType, String remark) {

        Path path = file.toPath();
        BasicFileAttributes attr = null;
        String taskStatus = "";
        if (Files.exists(path)) {
            try {
                attr = Files.readAttributes(path, BasicFileAttributes.class);
            } catch (IOException e) {
                taskStatus = "ERROR";
                remark = "Files.readAttributes error";
            }
        } else {
            taskStatus = "ERROR";
            remark = "File not exists";
        }

        long length = file.length();

        Long createTime = null;
        Long modifiedTime = null;
        if (attr != null) {
            createTime = attr.creationTime().toMillis();
            modifiedTime = attr.lastModifiedTime().toMillis();

        }
        String originalName = file.getName();
        String displayName = FileUtil.getFilenameWithoutExtension(originalName);
        String suffix = FileUtil.getExtension(originalName).toLowerCase(); // 小写统一
        String mimeType = getMimeType(suffix);  // video/mp4 / image/jpeg ...
        String storageType = "LOCAL";                    // 默认本地存储


        // 默认初始化字段
        String hash = FileSameCheckTool.calculateHashSHA256(file);          // 计算 SHA-256
        String code = hash;
        Integer version = 1;
        Integer status = 1;                               // 1=正常
        Integer referenceCount = 0;

        if (createTime == null) {
            return new SysFile(code, hash, originalName, displayName, suffix, mediaType, mimeType, storageType, path.toString(), length, version, status, referenceCount, remark, taskStatus);
        } else {
            return new SysFile(new Date(createTime), new Date(modifiedTime), code, hash, originalName, displayName, suffix, mediaType, mimeType, storageType, path.toString(), length, version, status, referenceCount, remark, taskStatus);
        }

    }

    /**
     * 根据文件扩展名返回 MIME 类型
     *
     * @param extension 文件扩展名（不带点），小写优先
     * @return 标准 MIME 类型
     */
    public static String getMimeType(String extension) {
        if (extension == null || extension.isEmpty()) {
            return EXTENSION_TO_MIME.get("default");
        }
        extension = extension.toLowerCase();
        return EXTENSION_TO_MIME.getOrDefault(extension, EXTENSION_TO_MIME.get("default"));
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getReferenceCount() {
        return referenceCount;
    }

    public void setReferenceCount(Integer referenceCount) {
        this.referenceCount = referenceCount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getMaturityLevel() {
        return maturityLevel;
    }

    public void setMaturityLevel(String maturityLevel) {
        this.maturityLevel = maturityLevel;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
}

