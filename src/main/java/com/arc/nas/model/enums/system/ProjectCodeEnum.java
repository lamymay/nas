package com.arc.nas.model.enums.system;


import com.arc.nas.model.enums.EnumNumberInterface;

/**
 * 内部错误码
 * 规定一定范围是一类错误，如：
 * 0001-0999
 * 1000-1999
 * 2000-2999
 * 3000-3999
 * 4000-4999
 * ...
 * 9000-9998
 * <p>
 * <p>
 * ：）
 * 2）注意： ####  看标题 ！！！
 *
 * @since 2018/04/10
 */
public enum ProjectCodeEnum implements EnumNumberInterface {
    //=====================================================
    //  3个特殊状态
    //=ResponseVo====================================================

    FAILURE(999, "失败"),
    UNKNOWN(0, "未知"),
    SUCCESS(200, "成功"),
    NULL(2, "NULL"),


    //=====================================================
    // 0001-0999 服务中的一些系统级别的异常   重大的错误
    //=====================================================
    // 预留10个微服务的key
    NULL_RESPONSE(0011, "微服务返回NULL"),
    CALL_ILLEGAL(0010, "非法调用"),
    FALLBACK_RESPONSE(0012, "微服务返回FALLBACK"),
    RESULT_NULL_EXCEPTION(0013, "空结果异常"),
    MS_ILLEGAL_ARGUMENT(0016, "微服务参数异常"),

    //=====================================================
    // 1000-1999    业务中的非特定错误
    //=====================================================
    ILLEGAL_PARAMETER(1000, "参数错误"),
    BEAN_COPY_EXCEPTION(1004, "bean拷贝异常，"),
    NULL_PARAMETER_ERROR(1001, "参数错误：参数为NULL!"),
    EMPTY_PARAMETER_ERROR(1002, "参数错误：参数为空!"),
    BEAN_COPIER_EXCEPTION(1003, "BEAN拷贝异常"),
    TYPE_EXCEPTION(1004, "类型错误"),
    SMS_EXCEPTION(1003, "SMS服务异常"),


    PAGE_NULL_RESPONSE(1005, "分页对象null异常"),

    UPLOAD_FAILURE(1101, "文件上传失败"),


    MODIFY_EXCEPTION(2005, "修改数据异常"),
    MODIFY_STATE_EXCEPTION(4009, "修改状态异常"),
    BINDING_DUPLICATE_EXCEPTION(4008, "重复绑定"),

    OVER_LENGTH(1104, "注释字数，限制100字符以内！"),

    //APP
    APP_SYNC_CONTACT_NO_ONE(1201, "无有效同步数据！"),
    APP_SYNC_CONTACT_UNKNOWN_TYPE(1202, "同步联系人错误，类型异常！"),
    APP_SYNC_CONTACT_USER_NOT_EXIST(1204, "同步联系人，用户身份主体非法（不存在）！"),

    //=====================================================
    // 2000-2999 数据库操作级别的异常
    //=====================================================
    INSERT_EXCEPTION(2002, "插入数据到数据库出错"),
    DELETE_EXCEPTION(2003, "检索数据库时候出错"),
    UPDATE_EXCEPTION(2003, "检索数据库时候出错"),
    SELECT_EXCEPTION(2003, "检索数据库时候出错"),

    //=====================================================
    // 3000-3999 网络请求操作级别的异常
    //=====================================================


    //=====================================================
    // 4000-4999 安全权限相关异常
    //=====================================================

    LOGIN_ERROR(4000, "登录时失败，原因：账号或者密码错误请重新登录!"),
    TOKEN_ERROR_EXPIRED(5, "签名不可用，原因：可能是登陆过期，请重新登录！"),
    INVALID_JWT(5, "签名不可用，原因：可能是登陆过期，请重新登录！"),
    SECURITY_ERROR(4000, "授权失败，原因：账号或者密码错误请重新登录!"),
    SECURITY_ERROR_JWT_FALSIFY(1000, "签名不可用，请重新登录,原因：伪造签名。Falsified authorization token  !"),
    SECURITY_ERROR_JWT_HEADER_MISSING(1000, "签名不可用，请重新登录,HTTP请求头参数缺少Authorization信息。Missing or invalid Authorization header!"),

    INVALID_PASSWORD(4001, "密码错误"),
    INVALID_USERNAME_USER_NOT_EXIST(4002, "账号错误,原因：用户不存在!"),
    INVALID_TOKEN(5, "签名不可用，原因：可能是登陆过期，请重新登录！"),

    VERIFY_EXCEPTION(1013, "验证失败,可能原因：您输入错了"),
    VERIFY_CODE_SYSTEM_SEND_EXCEPTION(4007, "发送验证码失败"),
    VERIFY_CODE_SEND_OVER_MAX_TIMES(4004, "发送验证码超过最大次数"),
    VERIFY_CODE_SEND_LESS_60S(4005, "发送验证码过于频繁"),
    CHANGE_PASSWORD_ERROR(4007, "密码修改错误"),


    // 备选 错误码
    CREATE_NEW_ORDER_NO_ERROR(1016, "生成新的订单号异常"),
    UNKNOWN_FLIGHT_CLASS_TYPE(1017, "未知机票舱等类型"),
    UNKNOWN_TRAIN_CLASS_TYPE(1018, "未知机票舱等类型"),
    GET_IDENTIFYING_CODE_ERROR(0017, "获取验证码失败，可能原因：手机号格式错误，不给发短信"),
    DELETE_TRAVEL_APPLY_FAILED(18, "删除出差申请单失败"),

    BOOKING_STATUS_ERROR(1025, "预定方式错误"),
    MENU_UPDATE_ERROR(1026, "菜单维护出错"),


    //=====================================================
    //    5000-5999 异常
    //=====================================================
    FILE_OPERATE_ERROR(5100, "文件操作错误"),
    FILE_DELETE_ERROR(5101, "文件操作错误"),
    FILE_NOT_EXIST_ERROR(5102, "错误，文件不存在"),
    FILE_SAVE_EXCEPTION(5103, "文件保存异常"),
    ILLEGAL_FILE(5104, "非法文件，原因：文件为空或者格式无法处理，提示：选择我们提供的模板填入数据后上传，不要修改文件名"),
    BINDING_EXCEPTION(5105, "绑定错误"),

    ;
    /**
     * 错误码
     */
    private int key;
    /**
     * 错误信息
     */
    private String message;


    ProjectCodeEnum(int key, String message) {
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

