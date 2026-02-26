package com.arc.nas.model.domain.system.common;

import com.arc.util.CodeUtil;
import com.arc.util.file.FileUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

import static com.arc.nas.model.constants.file.StorageType.LOCAL;


/**
 * 系统文件记录
 */
@TableName("sys_file")
public class SysFile implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Date createTime;// 创建时间
    private Date updateTime;// 更新时间
    private String code;//编号 db中不同行数据hash可以相同但是 code不相同
    private String hash;// sha256
    private String originalName;// 文件真实存在的名称（不含路径）
    private String displayName;// 显示名称（可为空，默认=originalName）
    private String mediaType;//  业务分类：视频/音频/图片  类型 文件还是图片 VIDEO/IMAGE/FILE/THUMBNAIL  一般来说图片是可以直接预览的,
    private String storageType;// LOCAL / OSS / NAS
    private String path;// 文件本地存放位置--本地存储的情况应该是服务器的绝对路径
    private Long length;// 文件大小 单位byte
    private Integer version;// 版本信息id
    private Integer referenceCount;// 引用计数 reference_count
    private String remark;// 描述｜错误简短描述
    private Integer status;// 逻辑删除用的标识 0=删除的不可使用的
    /*
    | 状态                  | 含义          |
    | -------------------- | -------------- |
    | PENDING              | 文件刚扫描，未处理|
    | THUMBNAIL_GENERATING | 缩略图生成中     |
    | THUMBNAIL_DONE       | 缩略图生成完成   |
    | TRANSCODE_PENDING    | 转码待处理      |
    | TRANSCODING          | 转码中          |
    | TRANSCODE_DONE       | 转码完成        |
    | HASH_DONE            | HASH完成        |
    | END                  | END            |
    | FAILED               | 处理失败（缩略图/转码失败） |
    */
    private String taskStatus;//标识系统对文件的处理流程
    private String thumbnail;// 多个缩略图 用英文逗号分割


    private String maturityLevel;//内容敏感度 “年龄分级” “PG-13”、“R”、“18+” 暴力、血腥等
    private Long duration;//持续时间
    private Integer tagCount;//标签计数
    private Integer onMount;    // 0:离线 1:在线（默认） (磁盘挂载状态)

    public SysFile() {

    }

    public static SysFile createSysFileSimple(File file, String taskStatus) {
        String remark = "";
        SysFile sysFile = new SysFile();
        if (file != null) {
            Path path = file.toPath();
            BasicFileAttributes attr = null;
            if (Files.exists(path)) {
                try {
                    attr = Files.readAttributes(path, BasicFileAttributes.class);
                } catch (IOException e) {
                    taskStatus = "ERROR";
                    remark = "Files.readAttributes error";
                }
                final String originalName = file.getName();
                sysFile.setPath(file.getAbsolutePath());
                sysFile.setOriginalName(originalName);
                sysFile.setMediaType(FileUtil.getFileType(originalName));
                sysFile.setDisplayName(FileUtil.getFilenameWithoutExtension(originalName));
                sysFile.setLength(file.length());
                if (attr != null) {
                    Long createTime = attr.creationTime().toMillis();
                    Long modifiedTime = attr.lastModifiedTime().toMillis();
                    sysFile.setCreateTime(new Date(createTime));
                    sysFile.setUpdateTime(new Date(modifiedTime));
                }
            } else {
                taskStatus = "ERROR";
                remark = "File not exists";
            }
        } else {
            taskStatus = "ERROR";
            remark = "File is null";
        }

        sysFile.setStorageType(LOCAL);//"LOCAL"
        sysFile.setVersion(1);
        sysFile.setStatus(1);// 1=正常
        sysFile.setReferenceCount(0);
        sysFile.setHash("");// skip计算 SHA-256
        sysFile.setCode(CodeUtil.createCode16());
        sysFile.setRemark(remark);
        sysFile.setTaskStatus(taskStatus);
        return sysFile;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
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

    public Integer getTagCount() {
        return tagCount;
    }

    public void setTagCount(Integer tagCount) {
        this.tagCount = tagCount;
    }

    public Integer getOnMount() {
        return onMount;
    }

    public void setOnMount(Integer onMount) {
        this.onMount = onMount;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }
}

