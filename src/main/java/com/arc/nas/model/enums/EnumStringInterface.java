package com.arc.nas.model.enums;

/**
 * 枚举类的泛化父类-注意key是字符串类型
 *
 * @since 2022
 */
public interface EnumStringInterface {


    /**
     * @return 枚举的编号
     */
    String getKey();

    /**
     * @return 枚举描述信息
     */
    String getMessage();

    /**
     * @return 枚举的标识-枚举类的类名称simpleClassName
     */
    String getTag();
}
