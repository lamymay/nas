package com.arc.nas.model.domain.system.common;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

/**
 * key-value表(偷懒用)
 * 注意 key是没有做唯一约束的, 要想定位唯一的一个键值对需要加上range
 *
 * @author may
 * @since 2021/04/14
 */
@TableName("sys_key_value")
public class KeyValue implements Serializable {


    /**
     * 自增id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Date createTime;// 创建时间
    private Date updateTime;// 更新时间
    /**
     * 键
     */
    @TableField(value = "`key`")
    private String key;

    /**
     * 值
     */
    @TableField(value = "`value`")
    private String value;

    /**
     * 适用范围,即类型
     */
    @TableField(value = "`range`")
    private String range;

    /**
     * 注释
     */
    private String remark;

    /**
     * 毫秒数
     */
    private long ttl;

    //    private EnableEnum enable;//枚举属性测试,非零=true=可用的/零=false=不可用(默认)
    //    private byte[] valueBinary;//值-二进制属性测试,blob类型

    public KeyValue() {

    }

    public KeyValue(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public KeyValue(String key, String value, String range) {
        this.key = key;
        this.value = value;
        this.range = range;
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }
}
