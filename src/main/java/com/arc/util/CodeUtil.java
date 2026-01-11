package com.arc.util;

import com.arc.nas.model.exception.BizException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @since 2020/5/10 2:54 下午
 */
public class CodeUtil {

    public static final String ONE_ONE = "11";
    public static final String mosaic = "*";
    private static final Map<String, List<String>> urlsMap = new HashMap<>();
    /**
     * 夺宝码的取值范围
     */
    public static String STR = "123456789";
    /**
     * 夺宝码的长度
     */
    public static int INIT_REDEMPTION_CODE_LENGTH = 8;
    static Random random = new Random();

    /**
     * 创建一个随机的字符串的code length=16
     *
     * @return 创建一个随机的字符串的code
     */
    public static String createCode16() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16).toUpperCase();
    }

    /**
     * 创建一个随机的字符串的code length=32
     *
     * @return 创建一个随机的字符串的code
     */
    public static String createCodeLength32() {
        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }

    public static String createAliUid() {
        return "U" + getUUID(15);
    }

    public static String getUUID(int maxLength) {
        if (maxLength < 1) {
            maxLength = 16;
        }
        if (maxLength > 64) {
            maxLength = 64;
        }
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, maxLength).toUpperCase();
    }

    public static String addMosaicNickname(String nickname) {
        int userNicknameTruncateLength = 20;
        if (nickname == null || StringUtils.isBlank(nickname)) {
            return mosaic + mosaic;
        } else if (nickname.length() == 1) {
            return nickname + mosaic;
        } else if (nickname.length() == 2) {
            return nickname.charAt(0) + mosaic;
        } else {
            int length = nickname.length();
            int tempLength = length - 2;
            if (tempLength > userNicknameTruncateLength) {
                tempLength = userNicknameTruncateLength;
            }
            int i = 0;
            StringBuffer buffer = new StringBuffer();
            while (i < tempLength) {
                buffer.append(mosaic);
                ++i;
            }
            return "" + nickname.charAt(0) + buffer + nickname.charAt(length - 1);
        }
    }

    /**
     * 从一个范围内获取一个随机数
     *
     * @param start 下限 (必须为非负数,包含在返回中中)
     * @param end   上限  ( 上限不包含在返回值中)
     * @return 随机数 范围是[start,end] 闭区间
     */
    public static long createRandomId(final long start, final long end) {
        if (start < 0) {
            throw new RuntimeException("下限值必须为非负数");
        }
        return org.apache.commons.lang3.RandomUtils.nextLong(start, end);
    }

    /**
     * @return 获取初始兑奖码 8位 有概率重复
     */
    public static int initRedemptionCode() {
        return Integer.valueOf(RandomStringUtils.random(INIT_REDEMPTION_CODE_LENGTH, STR));
    }

    /**
     * @return 获取初始兑奖码 8位 有概率重复
     */
    public static int initRedemptionCodeStartWithOne() {
        return Integer.valueOf(ONE_ONE + RandomStringUtils.random(INIT_REDEMPTION_CODE_LENGTH, STR).substring(2));
    }

    /**
     * 从一个范围内获取一个随机数
     *
     * @param start 下限 (必须为非负数,包含在返回中中)
     * @param end   上限  ( 上限不包含在返回值中)
     * @return 随机数 范围是[start,end) 区间
     */
    public static long createRandomNumber(final long start, final long end) {
        if (start < 0) {
            throw new RuntimeException("下限值必须为非负数");
        }
        return org.apache.commons.lang3.RandomUtils.nextLong(start, end);
    }

    /**
     * 随机返回url配置中的一个
     *
     * @param configUrlList URL字符串,多个URL请用英文逗号分割,前后无方括号
     * @return 随机返回url配置中的一个
     */
    public static String randomGetUrlFormList(String configUrlList) {
        if (StringUtils.isBlank(configUrlList)) {
            throw new RuntimeException("关键配置缺失,查询挂件的完整链接");
        }

        List<String> urlList = urlsMap.get(configUrlList);
        if (urlList == null) {
            // cache
            String[] split = configUrlList.split(",");
            if (split == null || split.length < 1) {
                throw new RuntimeException("关键配置为空,查询挂件的完整链接");
            }

            urlList = new ArrayList<>(split.length);
            for (String item : split) {
                if (StringUtils.isNotBlank(item)) urlList.add(item);
            }
            urlsMap.put(configUrlList, urlList);
        }


        // index该值介于 [0,length)
        return urlList.get(random.nextInt(urlList.size()));
    }

    private static void testGetRandom3() {
        int maxTime = 1000;
        int current = 0;
        while (current < maxTime) {
            current++;
            System.out.println(randomGetUrlFormList("A,B,"));
            //System.out.println(getQueryContentByGuaContentIdsUlrPrefix());
        }
    }

    static private String getQueryContentByGuaContentIdsUlrPrefix() {
        String queryGuaJianUrlList = "A,B,";
        if (StringUtils.isBlank(queryGuaJianUrlList)) {
            throw new BizException("关键配置缺失,查询挂件的完整链接");
        }
        String[] splitArray = queryGuaJianUrlList.split(",");
        if (splitArray == null || splitArray.length < 1) {
            throw new BizException("关键配置为空,查询挂件的完整链接");
        }

        //该值介于 [0,n)
        return splitArray[random.nextInt(splitArray.length)];
    }

    private static void testGetRandom2() {

        Random random = new Random();
        int maxTime = 1000;
        int current = 0;
        while (current < maxTime) {
            current++;
            System.out.println(random.nextInt(2));
        }
    }

    private static void testGetRandom() {
        // 测试活动 1589  7274274746FD4587   duob_IT_7274274746FD4587   _NDkI0333kIqLEJCoeEj0w

        //************************************************************************************************************************************
        //"extend": "{\"WINNING_USER_RECORD_COUNT\":1,\"REDEMPTION_CODE_LOW_LIMIT\":11986230,\"TOTAL_TIME_OF_CUMULATIVE_PARTICIPANTS\":2,\"TOTAL_NUMBER_OF_CUMULATIVE_PARTICIPANTS\":2}",
        //{"extend":"{"REDEMPTION_CODE_LOW_LIMIT":11627757}"}
        //1533 AAD301720C324412 "extend": "{\"WINNING_USER_RECORD_COUNT\":1,\"REDEMPTION_CODE_LOW_LIMIT\":11986230,\"TOTAL_TIME_OF_CUMULATIVE_PARTICIPANTS\":2,\"TOTAL_NUMBER_OF_CUMULATIVE_PARTICIPANTS\":2}",

        // duob_ITPS_C5A97A538E6A43FA       奖池缓存key
        // duob_IT_C5A97A538E6A43FA         夺宝码incr的key
        // duob_CGIBC_29E89815D0344C3B      测试取出实例的缓存 for cache selectByInstanceCode


        // 1 更新extend字段  id=1533  value= {"id":1533,"extend":"{\"REDEMPTION_CODE_LOW_LIMIT\":12323465}"}
        // 2 更新缓存 key=duob_IT_AAD301720C324412  value=12323466  [12323465 12323466 12323467]  当前最大值=12479792
        // 3 之后需还原数据 update db
        // 4 update cache


        //duob_IT_18DF34E81AC2442F
        System.out.println("############################################################# ?11");
        System.out.println(CodeUtil.createRandomNumber(12323465, 12323465 + 1)); //12323466

        HashMap<String, Object> map = new HashMap<>();
        map.put("REDEMPTION_CODE_LOW_LIMIT", 12323465);
        String temp = "{\"REDEMPTION_CODE_LOW_LIMIT\":12323465}";
        System.out.println("############################################################# SW");
        System.out.println(CodeUtil.createRandomNumber(13141195, 13141195 + 1)); //13141195
        System.out.println(CodeUtil.createRandomNumber(13141195, 13141195 + 1)); //13141195
        System.out.println(CodeUtil.createRandomNumber(13141195, 13141195 + 1)); //13141195
        System.out.println(CodeUtil.createRandomNumber(13141195, 13141195 + 1)); //13141195
        System.out.println(CodeUtil.createRandomNumber(13141195, 13141195 + 1)); //13141195

        System.out.println("############################################################# ?");

        //************************************************************************************************************************************

    }

    private static void testAddMosaicNickname() {

        System.out.println(createAliUid());
        System.out.println(createAliUid());
        System.out.println(createAliUid());
        System.out.println(createAliUid());

        System.out.println(DateUtils.addHours(new Date(), -1));
        System.out.println(DateUtils.addHours(new Date(), -10));

        String nickname = "A";
        System.out.println(nickname.charAt(0));
        System.out.println("首字母=" + nickname.charAt(0));
        System.out.println("尾字母=" + nickname.substring(nickname.length() - 1));

        nickname = "AB";
        System.out.println(nickname.charAt(0));
        System.out.println("首字母=" + nickname.charAt(0));
        System.out.println("尾字母=" + nickname.substring(nickname.length() - 1));


        nickname = "ABC";
        System.out.println(nickname.charAt(0));

        System.out.println("首字母=" + nickname.charAt(0));
        System.out.println("尾字母=" + nickname.substring(nickname.length() - 1));

        nickname = "ABCD";
        System.out.println(nickname.charAt(0));
        System.out.println("首字母=" + nickname.charAt(0));
        System.out.println("尾字母=" + nickname.substring(nickname.length() - 1));


        System.out.println(addMosaicNickname(null));
        System.out.println(addMosaicNickname(""));
        System.out.println(addMosaicNickname("   "));
        System.out.println(addMosaicNickname("张"));
        System.out.println(addMosaicNickname("张三"));
        System.out.println(addMosaicNickname("张三哈"));
        System.out.println(addMosaicNickname("赵子龙"));
        System.out.println(addMosaicNickname("撒豆腐阿斯达"));
        System.out.println(addMosaicNickname("撒豆腐阿大师傅法撒旦是非得失而又特容易斯达"));

    }

    public static void testCreateRandomId() {
        int max = 10000;
        int current = 0;

        int zero = 0;
        int one = 0;
        while (current < max) {
            current = current + 1;
            long randomId = createRandomId(0, 2);
            if (randomId == 0) {
                zero = zero + 1;
            }
            if (randomId == 1) {
                one = one + 1;
            }
        }
        System.out.println("测试一,zero=" + zero + "one=" + one);
        System.out.println("测试二:" + createRandomId(85547829, 95547829));

    }

    /**
     * 生成带字母的随机数     指定长度
     *
     * @param base
     * @param length
     * @return
     */
    public static String getRandomString(String base, int length) { //length表示生成字符串的长度
        Random random = new Random(System.currentTimeMillis());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        //testCreateRandomId();
        //testAddMosaicNickname();
//        testGetRandom();
        //testGetRandom3();
//        System.out.println(randomGetUrlFormList("A,B"));
//        System.out.println(randomGetUrlFormList("A,B,,C"));
//        System.out.println(randomGetUrlFormList(""));

        //System.out.println(randomGetUrlFormList(","));// Error

//        System.out.println(randomGetUrlFormList("  "));
        System.out.println(randomGetUrlFormList(null));

    }

    /**
     * 获取现在时间字符串
     */
    private static String getStringDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        return formatter.format(currentTime);
    }

    /**
     * 简单流水号生成：
     * 由年月日时分秒+3位随机数
     */
    public static String getSimpleCode() {
        String t = getStringDate();
        int x = (int) (Math.random() * 900) + 100;
        return t + x;
    }

    //生成流水号
    public String getSerialNumber() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String format = formatter.format(new Date());
        long tem = (long) (Math.random() * 1000000);//6位随机数
        return format += tem;
    }

}


