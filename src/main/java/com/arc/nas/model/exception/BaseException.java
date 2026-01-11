package com.arc.nas.model.exception;


/**
 * 基础异常
 *
 * @author may
 * 2019/04/10  12:20
 */
public interface BaseException {

    /**
     * 获取错误码
     *
     * @return
     */
    Integer getCode();

    /**
     * 获取错误信息
     *
     * @return
     */
    String getMsg();

}
