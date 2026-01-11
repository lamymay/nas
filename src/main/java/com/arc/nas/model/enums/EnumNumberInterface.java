package com.arc.nas.model.enums;

/**
 * 枚举类的泛化父类-注意key是数字类型
 *
 * @author may
 * @since 2021.08.17 11:45 上午
 */
public interface EnumNumberInterface {


    /**
     * @return 枚举的编号
     */
    int getKey();

    /**
     * @return 枚举描述信息
     */
    String getMessage();

    /**
     * @return 枚举的标识-枚举类的类名称simpleClassName
     */
    String getTag();
}
