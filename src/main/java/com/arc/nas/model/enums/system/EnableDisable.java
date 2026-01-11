package com.arc.nas.model.enums.system;


import com.arc.nas.model.enums.EnumNumberInterface;

/**
 * 只有可用不可用两种状态的枚举类
 *
 * @author may
 */
public enum EnableDisable implements EnumNumberInterface {

    /**
     * 可用
     */
    ENABLE(1, "可用"),

    /**
     * 不可用
     */
    DISABLE(0, "不可用"),
    ;

    private int key;

    private String message;

    EnableDisable(int key, String message) {
        this.key = key;
        this.message = message;
    }

    @Override
    public int getKey() {
        return key;
    }

    public void setFlag(int key) {
        this.key = key;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getTag() {
        return this.getClass().getSimpleName();
    }
}
