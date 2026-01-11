package com.arc.nas.init;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import static com.arc.nas.init.GetLocalIPAddress.localNetAddress;

/**
 * 初始化相关操作
 *
 */
public class ReadyResourceInit {

    /**
     * 临时目录，注意你电脑上是否有该目录
     * 配置的项目文件输出目录，注意你电脑上是否有该目录
     * 读取配置文件 @Value("${arc.file.upload.path:/data/upload}")
     */
    public static File writeableDirectory;

    static String osName = System.getProperty("os.name") == null ? "" : System.getProperty("os.name");

    static {
        //final String tmpFilePath = System.getProperty("java.io.tmpdir") + File.separator + "image_backup";
        String writeableDirectoryFormConfigFilePath = String.valueOf(loadConfigMap().get("arc.file.upload.path"));
        if (StringUtils.isBlank(writeableDirectoryFormConfigFilePath)) {
            if (osName.toUpperCase().contains("MAC")) {
                writeableDirectory = new File("/Users/may/Desktop/nas_app_upload");
                // WIN Linux 上遇到问题了重新配置           } else if (osName.toUpperCase().contains("WINDOWS")) {tmpFile = new File("/nas_app_upload");
            } else {
                throw new RuntimeException("临时目录不存在");
            }

            if (writeableDirectory.exists()) {
                System.out.println("\033[35;4m" + "配置writeableDirectory\n" + writeableDirectory + "\n\033[0m");

            } else {
                boolean mkdirs = writeableDirectory.mkdirs();
                System.out.println("\033[35;4m" + "配置writeableDirectory" + (mkdirs ? "成功" : "失败") + "\n" + writeableDirectory + "\n\033[0m");

            }

            if (!writeableDirectory.exists() || !writeableDirectory.canWrite()) {
                throw new RuntimeException("必要配置不存在,项目的文件输出目录未设置,或者目录不可写!");
            }
        } else {
            writeableDirectory = new File(".");

        }


    }

    // todo  loadConfigMap
    private static Map<String, Object> loadConfigMap() {
//        Map<String, Object> configMap = PropertiesYmlResourceParser.loadProperties();
        // yaml
        return Collections.emptyMap();
    }

    /**
     * 初始化文件上传的输出文件夹
     */
    public static void init() {
        ((Runnable) () -> {
            System.out.println("\033[35;4m" + writeableDirectory + "\n\033[0m");
            System.out.println("\033[35;4m" + "程序运行在的系统是:" + osName + "\n\033[0m");
            System.out.println("\033[35;4m" + "localNetAddress:" + localNetAddress + "\n\033[0m");
//            initUploadOutDirectory();
//        log.info("项目运行在的系统是unix={},os={},配置的项目文件输出目录,配置的项目文件输出目录耗时{},uploadPathDefault={},uploadPath={}", isUnix, System.getProperty("os.name"), StringUtil.getTimeStringSoFar(t1), uploadPath, uploadPathDefault);
        }).run();
    }


    public static File getWriteableDirectory() {
        return writeableDirectory;
    }

    @Deprecated
    public static void setWriteableDirectory(File writeableDirectory) {
        ReadyResourceInit.writeableDirectory = writeableDirectory;
    }



}
