package com.arc.nas.model.domain.app.media;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

// tag到媒体文件的关系表
@TableName("media_file_tag_relation")
public class FileTagRelation {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String fileCode;
    private String tagCode;

    public FileTagRelation() {
    }

    public FileTagRelation(String fileCode, String tagCode) {
        this.fileCode = fileCode;
        this.tagCode = tagCode;
    }

    public String getFileCode() {
        return fileCode;
    }

    public void setFileCode(String fileCode) {
        this.fileCode = fileCode;
    }

    public String getTagCode() {
        return tagCode;
    }

    public void setTagCode(String tagCode) {
        this.tagCode = tagCode;
    }
}
