package com.arc.nas.model.request.system.dictionary;

/**
 * @since 2019/9/29 23:28
 */
public class SysDataDictionaryRequest {
    private Long id;//主键
    private String name;//名称

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
