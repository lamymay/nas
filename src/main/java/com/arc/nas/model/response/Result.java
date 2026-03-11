package com.arc.nas.model.response;


import com.arc.nas.model.enums.system.ProjectCodeEnum;

import java.io.Serializable;

/**
 * 该类对controller返回值做了统一封装
 * 返回数据的数据具有一致的格式
 */
public class Result<T> implements Serializable {

    private int code;
    private String message;
    private T data;
    private String traceId;

    //构造器
    public Result() {
    }

    public Result(T data) {
        this.data = data;
    }

    public Result(ProjectCodeEnum projectCode) {
        this.code = projectCode.getKey();
        this.message = projectCode.getMessage();
        this.data = null;
    }

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    //success方法
    public static <T> Result<T> success() {
        return new Result<T>(ProjectCodeEnum.SUCCESS.getKey(), ProjectCodeEnum.SUCCESS.getMessage(), null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<T>(ProjectCodeEnum.SUCCESS.getKey(), ProjectCodeEnum.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> success(ProjectCodeEnum enumCode) {
        return new Result<T>(enumCode.getKey(), enumCode.getMessage(), null);
    }

    public static <T> Result<T> success(ProjectCodeEnum enumCode, T data) {
        return new Result<T>(enumCode.getKey(), enumCode.getMessage(), data);
    }

    //失败
    public static <T> Result<T> failure(ProjectCodeEnum enumCode) {
        return new Result<T>(enumCode.getKey(), enumCode.getMessage(), null);
    }

    public static <T> Result<T> failure(T data) {
        return new Result<T>(ProjectCodeEnum.FAILURE.getKey(), ProjectCodeEnum.FAILURE.getMessage(), data);
    }

    public static <T> Result<T> failure(ProjectCodeEnum enumCode, T data) {
        return new Result<T>(enumCode.getKey(), enumCode.getMessage(), data);
    }

    public static Result failure() {
        return new Result(ProjectCodeEnum.FAILURE);
    }

    public static Result failure(int code, String message) {
        return new Result(code, message, ProjectCodeEnum.FAILURE);
    }

    public boolean isSuccess() {
        return (this.code >= 200 && this.code <= 299);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
