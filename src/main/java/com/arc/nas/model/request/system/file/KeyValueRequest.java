package com.arc.nas.model.request.system.file;


/**
 * kv 分页入口参数
 *
 * @author may
 */
public class KeyValueRequest {//extends ArcPageable {


    private Long id;//自增主键
    private String key;//key
    private String value;//值-字符串格式

    private byte[] valueBinary;//列值 blob类型
    private Integer range;//适用范围,即类型

    private String remark;//注释

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public byte[] getValueBinary() {
        return valueBinary;
    }

    public void setValueBinary(byte[] valueBinary) {
        this.valueBinary = valueBinary;
    }

    public Integer getRange() {
        return range;
    }

    public void setRange(Integer range) {
        this.range = range;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

}
