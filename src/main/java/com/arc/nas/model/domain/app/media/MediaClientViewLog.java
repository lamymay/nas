package com.arc.nas.model.domain.app.media;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("media_client_view_log")
public class MediaClientViewLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long playAt;
    private String clientCode;
    private String fileCode;

    public MediaClientViewLog() {
    }

    public MediaClientViewLog(Long playAt,String clientCode , String fileCode) {
        this.playAt = playAt;
        this.clientCode = clientCode;
        this.fileCode = fileCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getViewTime() {
        return playAt;
    }

    public void setViewTime(Long playAt) {
        this.playAt = playAt;
    }

    public String getClientCode() {
        return clientCode;
    }

    public void setClientCode(String clientCode) {
        this.clientCode = clientCode;
    }

    public String getFileCode() {
        return fileCode;
    }

    public void setFileCode(String fileCode) {
        this.fileCode = fileCode;
    }
}
