package com.arc.nas.model.exception;


import com.arc.nas.model.enums.system.ProjectCodeEnum;

import java.net.UnknownHostException;

/**
 * 业务运行时异常
 *
 * @author may
 * @since 2018/04/10
 */
public class BizException extends RuntimeException implements BaseException {


    private int code = ProjectCodeEnum.UNKNOWN.getKey();

    private String msg = ProjectCodeEnum.UNKNOWN.getMessage();

    public BizException() {
        super();
    }

    public BizException(ProjectCodeEnum projectCode) {
        super(projectCode.getMessage() + projectCode.getKey());
        this.code = projectCode.getKey();
        this.msg = projectCode.getMessage();
    }

    public BizException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public BizException(Exception e, ProjectCodeEnum projectCode) {
        super(e);
        this.code = projectCode.getKey();
        this.msg = projectCode.getMessage();
    }

    public BizException(ProjectCodeEnum projectCode, Exception e) {
        super(e);
        this.code = projectCode.getKey();
        this.msg = projectCode.getMessage();
    }

    public BizException(UnknownHostException e) {
        super(e);
    }

    public static void main(String[] args) {
        //logAdapter("log name={}, key={}", "abc", "var");
        testException();
    }

    private static void testException() {
        throw new BizException("我是错误信息");
    }

    public static void logAdapter(String template, String... messages) {
        if (template == null) return;

        if (template.contains("{}")) {
            template = template.replaceAll("\\{\\}", "%s");
        }

        String message = String.format(template, messages);
        System.out.println(message);


    }

    public Integer getKey() {
        return this.code;
    }

    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getMsg() {
        return this.msg;
    }
}
