package com.arc.util.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileDisplayHumanTool {

    static final double kByte = 1024;
    static final double mByte = 1024 * kByte;
    static final double gByte = 1024 * mByte;
    static final double tByte = 1024 * gByte;
    static final double eByte = 1024 * tByte;
    static final double pByte = 1024 * eByte;
    private static final Logger log = LoggerFactory.getLogger(FileDisplayHumanTool.class);

    public static String formatFileLengthWithUnit(long length) {

        double humanNumber = 0D;
        String humanNumberUnit = "";
        if (length < kByte) {
            humanNumber = length;
            humanNumberUnit = "B";
        } else if (length < mByte) {
            humanNumber = length / kByte;
            humanNumberUnit = "KB";
        } else if (length < gByte) {
            humanNumber = length / mByte;
            humanNumberUnit = "MB";
        } else if (length < tByte) {
            humanNumber = length / gByte;
            humanNumberUnit = "GB";
        } else if (length < eByte) {
            humanNumber = length / tByte;
            humanNumberUnit = "TB";
        } else if (length < pByte) {
            humanNumber = length / eByte;
            humanNumberUnit = "TB";
        } else {
            humanNumberUnit = "?";
        }
        // %.2f %. 表示 小数点前任意位数 2 表示两位小数 格式后的结果为f 表示浮点型。
        return String.format("%.2f%s", humanNumber, humanNumberUnit);

    }

    public static String[] formatFileLengthWithUnitV3(long length) {

        double humanNumber = 0D;
        String humanNumberUnit = "";
        if (length < kByte) {
            humanNumber = length;
            humanNumberUnit = "B";
        } else if (length < mByte) {
            humanNumber = length / kByte;
            humanNumberUnit = "KB";
        } else if (length < gByte) {
            humanNumber = length / mByte;
            humanNumberUnit = "MB";
        } else if (length < tByte) {
            humanNumber = length / gByte;
            humanNumberUnit = "GB";
        } else if (length < eByte) {
            humanNumber = length / tByte;
            humanNumberUnit = "TB";
        } else if (length < pByte) {
            humanNumber = length / eByte;
            humanNumberUnit = "TB";
        } else {
            humanNumberUnit = "?";
        }
        // %.2f %. 表示 小数点前任意位数 2 表示两位小数 格式后的结果为f 表示浮点型。
        String format = String.format("%.2f", humanNumber);
        return new String[]{format, humanNumberUnit};

    }


    @Deprecated
    public static String getFileSizeWithUnitV0(long length) {
        double kByte = 1024;
        double mByte = 1024 * kByte;
        double gByte = 1024 * mByte;
        double tByte = 1024 * gByte;
        double eByte = 1024 * tByte;
        double pByte = 1024 * eByte;


        double humanNumber = 0D;
        String humanNumberUnit = "";
        if (length < kByte) {
            humanNumber = length;
            humanNumberUnit = "B";
        } else if (length < mByte) {
            humanNumber = length / kByte;
            humanNumberUnit = "KB";
        } else if (length < gByte) {
            humanNumber = length / mByte;
            humanNumberUnit = "MB";
        } else if (length < tByte) {
            humanNumber = length / gByte;
            humanNumberUnit = "GB";
        } else if (length < eByte) {
            humanNumber = length / tByte;
            humanNumberUnit = "TB";
        } else if (length < pByte) {
            humanNumber = length / eByte;
            humanNumberUnit = "TB";
        } else {
            humanNumberUnit = "?";
        }
        // %.2f %. 表示 小数点前任意位数 2 表示两位小数 格式后的结果为f 表示浮点型。
        return String.format("%.2f%s", humanNumber, humanNumberUnit);

    }


    /**
     * 文件大小转换为MB级别单位显示
     *
     * @param length 输入参数是单位是 B(bytes)
     * @return Megabytes 精度8位小数
     */
    public static double formatFileLengthUseMegabytesUnit(long length) {
        double megabytes = (double) length / (1024 * 1024);
        // 保留8位小数
        return Math.round(megabytes * 100000000.0) / 100000000.0;
    }

    /**
     * 判断文件是MB级别 还是GB级别  B KB MB GB TB EB
     *
     * @param length length
     * @return 单位
     */
    public static String formatFileLengthOnlyGetUnit(long length) {
        double kByte = 1024;
        double mByte = 1024 * kByte;
        double gByte = 1024 * mByte;
        double tByte = 1024 * gByte;
        double eByte = 1024 * tByte;

        if (length < kByte) {
            return "B";
        } else if (length < mByte) {
            return "KB";
        } else if (length < gByte) {
            return "MB";
        } else if (length < tByte) {
            return "GB";
        } else if (length < eByte) {
            return "TB";
        }
        return "我也不知道";
    }


}
