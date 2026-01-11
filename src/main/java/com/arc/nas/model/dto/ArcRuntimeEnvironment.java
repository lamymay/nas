package com.arc.nas.model.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


/**
 * 项目运行时环境数据封装
 *
 * @author may
 * @since 2021/4/14 14:39
 */
public class ArcRuntimeEnvironment {

    private static final Logger log = LoggerFactory.getLogger(ArcRuntimeEnvironment.class);

    java.util.Map<String, String> env;
    private String os;
    private String path;
    private String username;//用户名
    private String computerName;//名 USERDOMAIN = 用户域USERDNSDOMAIN
    private String userProfile;//名
    private String allUsersProfile;//用户公共目录 ALLUSERSPROFILE
    private String runnableSuffix;//可执行后缀
    private String classpath;//classpath
    private String javaHome;//JAVA_HOME
    private String temp;//用户临时文件目录TEMP
    private String systemDrive;//系统盘符
    private String defaultProgramFiles;//默认程序目录
    private String processorArchitecture;//处理器体系结构 PROCESSOR_ARCHITECTURE
    private String processorLevel;//处理级别 PROCESSOR_LEVEL
    private String systemRoot;//SystemRoot 系统启动目录
    private String comSpec;//ComSpec 命令行解释器可执行程序的准确路径
    private String appData;//APPDATA 应用程序数据目录
    private String javaVersion;//java.version

    private ArcRuntimeEnvironment() {
    }

    public static ArcRuntimeEnvironment getArcRuntimeEnvironment() {
        return ArcSingletonEnum.SINGLETON.getArcRuntimeEnvironment();
    }


    public Map<String, String> getEnv() {
        return env;
    }

    public void setEnv(Map<String, String> env) {
        this.env = env;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(String computerName) {
        this.computerName = computerName;
    }

    public String getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(String userProfile) {
        this.userProfile = userProfile;
    }

    public String getAllUsersProfile() {
        return allUsersProfile;
    }

    public void setAllUsersProfile(String allUsersProfile) {
        this.allUsersProfile = allUsersProfile;
    }

    public String getRunnableSuffix() {
        return runnableSuffix;
    }

    public void setRunnableSuffix(String runnableSuffix) {
        this.runnableSuffix = runnableSuffix;
    }

    public String getClasspath() {
        return classpath;
    }

    public void setClasspath(String classpath) {
        this.classpath = classpath;
    }

    public String getJavaHome() {
        return javaHome;
    }

    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getSystemDrive() {
        return systemDrive;
    }

    public void setSystemDrive(String systemDrive) {
        this.systemDrive = systemDrive;
    }

    public String getDefaultProgramFiles() {
        return defaultProgramFiles;
    }

    public void setDefaultProgramFiles(String defaultProgramFiles) {
        this.defaultProgramFiles = defaultProgramFiles;
    }

    public String getProcessorArchitecture() {
        return processorArchitecture;
    }

    public void setProcessorArchitecture(String processorArchitecture) {
        this.processorArchitecture = processorArchitecture;
    }

    public String getProcessorLevel() {
        return processorLevel;
    }

    public void setProcessorLevel(String processorLevel) {
        this.processorLevel = processorLevel;
    }

    public String getSystemRoot() {
        return systemRoot;
    }

    public void setSystemRoot(String systemRoot) {
        this.systemRoot = systemRoot;
    }

    public String getComSpec() {
        return comSpec;
    }

    public void setComSpec(String comSpec) {
        this.comSpec = comSpec;
    }

    public String getAppData() {
        return appData;
    }

    public void setAppData(String appData) {
        this.appData = appData;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    enum ArcSingletonEnum {
        SINGLETON;

        private final ArcRuntimeEnvironment arcRuntimeEnvironment;

        ArcSingletonEnum() {
            arcRuntimeEnvironment = new ArcRuntimeEnvironment();
            arcRuntimeEnvironment.setEnv(System.getenv());
            arcRuntimeEnvironment.setOs(System.getenv("OS"));
            arcRuntimeEnvironment.setPath(System.getenv("Path"));
            arcRuntimeEnvironment.setUsername(System.getenv("USERNAME"));
            arcRuntimeEnvironment.setComputerName(System.getenv("COMPUTERNAME"));
            arcRuntimeEnvironment.setUserProfile(System.getenv("USERPROFILE"));
            arcRuntimeEnvironment.setAllUsersProfile(System.getenv("ALLUSERSPROFILE"));
            arcRuntimeEnvironment.setRunnableSuffix(System.getenv("PATHEXT"));
            arcRuntimeEnvironment.setClasspath(System.getenv("classpath"));
            arcRuntimeEnvironment.setJavaHome(System.getenv("JAVA_HOME"));
            arcRuntimeEnvironment.setTemp(System.getenv("TEMP"));
            arcRuntimeEnvironment.setSystemDrive(System.getenv("SystemDrive"));
            arcRuntimeEnvironment.setDefaultProgramFiles(System.getenv("ProgramFiles"));
            arcRuntimeEnvironment.setProcessorArchitecture(System.getenv("PROCESSOR_ARCHITECTURE"));
            arcRuntimeEnvironment.setProcessorLevel(System.getenv("PROCESSOR_LEVEL"));
            arcRuntimeEnvironment.setSystemRoot(System.getenv("SystemRoot"));
            arcRuntimeEnvironment.setComSpec(System.getenv("ComSpec"));
            arcRuntimeEnvironment.setAppData(System.getenv("APPDATA"));
            arcRuntimeEnvironment.setJavaVersion(System.getenv("java.version"));
        }

        public ArcRuntimeEnvironment getArcRuntimeEnvironment() {
            return arcRuntimeEnvironment;
        }


    }
}
//线程安全的，是延迟加载的

//此种单例,枚举类已经实现了单例，这种方法更加简单。

//枚举实现：
//
//下面这段代码就是声明枚举实例的通常做法，
// 它可能还包含实例变量和实例方法，但是为了简单起见，我并没有使用这些东西，仅仅需要小心的是如果你正在使用实例方法，
// 那么你需要确保线程安全（如果它影响到其他对象的状态的话）。默认枚举实例的创建是线程安全的，但是在枚举中的其他任何方法由程序员自己负责。

//System.getProperty()
//
//java.version Java ：运行时环境版本
//java.vendor Java ：运行时环境供应商
//java.vendor.url ：Java供应商的 URL
//java.home &nbsp;&nbsp;：Java安装目录
//java.vm.specification.version： Java虚拟机规范版本
//java.vm.specification.vendor ：Java虚拟机规范供应商
//java.vm.specification.name &nbsp; ：Java虚拟机规范名称
//java.vm.version ：Java虚拟机实现版本
//java.vm.vendor ：Java虚拟机实现供应商
//java.vm.name&nbsp; ：Java虚拟机实现名称
//java.specification.version：Java运行时环境规范版本
//java.specification.vendor：Java运行时环境规范供应商
//java.specification.name ：Java运行时环境规范名称
//java.class.version ：Java类格式版本号
//java.class.path ：Java类路径
//java.library.path  ：加载库时搜索的路径列表
//java.io.tmpdir  ：默认的临时文件路径
//java.compiler  ：要使用的 JIT编译器的名称
//java.ext.dirs ：一个或多个扩展目录的路径
//os.name ：操作系统的名称
//os.arch  ：操作系统的架构
//os.version  ：操作系统的版本
//file.separator ：文件分隔符
//path.separator ：路径分隔符
//line.separator ：行分隔符
//user.name ：用户的账户名称
//user.home ：用户的主目录
//user.dir：用户的当前工作目录

