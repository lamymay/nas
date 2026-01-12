package com.arc.util.file;

import com.arc.util.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static com.arc.util.file.FileUtil.FileUtilConst.*;


/**
 * 文件数据
 */
public class FileUtil {

    //***************************************************
    //                   工具方法1- 静态属性定义
    //***************************************************
    static final double kByte = 1024;
    static final double mByte = 1024 * kByte;
    static final double gByte = 1024 * mByte;
    static final double tByte = 1024 * gByte;
    static final double eByte = 1024 * tByte;
    static final double pByte = 1024 * eByte;
    private static final Logger log = LoggerFactory.getLogger(FileUtil.class);
    static Desktop desktop;
    static boolean enableDebug = true;
    static String osName = System.getProperty("os.name");

    static {
        try {
            if (!GraphicsEnvironment.isHeadless()) {
                desktop = Desktop.getDesktop();
            }
        } catch (Exception e) {
            log.warn("Desktop not available, running in headless mode", e);
        }
    }

    public static File createFileIfNotExist(File canWriteFile) {
        if (canWriteFile == null) {
            return null;
        }

        if (canWriteFile.exists() && canWriteFile.canWrite()) {
            return canWriteFile;
        }

        try {
//            if (canWriteFile.isFile()) {
            File parentFile = canWriteFile.getParentFile();
            if (parentFile != null && !parentFile.exists() && parentFile.isDirectory()) {
                boolean mkdirs = parentFile.mkdirs();
                log.info("输出文件夹={},mkdirs={}", parentFile.getAbsoluteFile(), mkdirs);
            }
            log.info("输出文件={}", canWriteFile.getAbsoluteFile());
            boolean newFile = canWriteFile.createNewFile();
            if (!newFile) {
                log.warn("文件创建失败 createNewFile() response ={},file:{}", newFile, canWriteFile);
            }
//            }
//            if (canWriteFile.isDirectory()) {
//                boolean mkdirs = canWriteFile.mkdirs();
//                if (!mkdirs) {
//                    log.warn("文件夹创建失败 mkdirs() response ={},file:{}", mkdirs, canWriteFile);
//                }
//            }

        } catch (Exception exception) {
            throw new RuntimeException("文件创建失败,file=" + canWriteFile, exception);
        }
        return canWriteFile;

    }

    public static OutputStream getFileOutputStream(String fullFileName) {

        log.info("##############################");
        log.info("   输出文件全路径={}", fullFileName);
        log.info("##############################");
        File file = new File(fullFileName);
        FileOutputStream outputStream = null;
        try {
            String parent = file.getParent();
            File parentFile = new File(parent);
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            outputStream = new FileOutputStream(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputStream;
    }

    /**
     * 反斜杠转换为斜杠
     *
     * @param path path
     * @return String
     */
    public static String convertToUnixSeparator(String path) {

        if (isBlank(path)) {
            return path;
        }

        /*
        WIN不允许文件名称出现 \ / : * ? " < > |
        说明符号 * 是路径分割符(Windows的路径分隔符是反斜杠，但处理文件的API接受带正斜杠的路径名)。
        符号 < > 是输入输出重定向，比如想把foo.exe的输出重定向到文件abc.txt:：foo > abc.txt；把anc.txt的内容输入给foo.exe：foo < abc.txt。
        符号 : 是用来区分盘符,比如C: D:。
        符号 "" 是用来标记带空格的路径，比如"C:\Program Files"。
        符号 | 是管道，把一个程序的输出作为另一个程序的输入，比如type命令查看文件内容，但如果文件很大一屏显示不下的话就需要把输出通过管道给more命令，这样每输出满一屏就会停下来直到你按键再输出下一屏： type abc.txt | more。

        PS0 NTFS文件系统不允许在根目录中存在以下文件名：$Mft，$MftMirr，$LogFile，$Volume，$AttrDef，$Bitmap，$Boot，$BadClus，$Secure，$Upcase，$Extend，$Quota，$ObjId，$Reparse。因为这些是NTFS文件系统的元文件
        PS1：Windows不允许只包含点的文件名，比如.，..，...等等，因为Windows中.代表当前文件夹而..代表上一级文件夹。
        PS2：NTFS文件系统不允许在根目录中存在以下文件名：$Mft，$MftMirr，$LogFile，$Volume，$AttrDef，$Bitmap，$Boot，$BadClus，$Secure，$Upcase，$Extend，$Quota，$ObjId，$Reparse。因为这些是NTFS文件系统的元文件。
        PS3：Windows不允许下列文件名：CON，PRN，AUX, NUL，COM1，COM2，COM3，COM4，LPT1，LPT2，LPT3，LPT4。因为这些名字是DOS和Windows中的设备文件名。比如CON输入时代表键盘，输出时代表屏幕；AUX代表辅助设备（通常是COM1），PRN代表打印机，NUL代表空设备，COMX代表COM接口，LPTX代表LPT接口。
        除了这把个字符，Windows还不允许文件名包含空字符(NULL,U+0000)。

        */

        /*
        mac 上测试件名称可以包含字符串 \\ /
        */

        if (isWindow()) {
            return path.replaceAll(WINDOWS_SEPARATOR, FileUtilConst.UNIX_SEPARATOR);
        }
        return path;

    }

    public static boolean isWindow() {
        //return (SystemOSUtil.isWindow()); // 有工具类的 但是这里为了高内聚，放弃使用外部依赖，直接调用工具类
        return (osName != null && osName.toUpperCase().contains("WINDOWS"));
    }

    /**
     * 提取目录，若不存在则创建
     * extractDirAndCreate
     *
     * @param filePath 文件或者 String
     * @return boolean
     */
    public static File verifyFileDirIfNotExistCreate(String filePath) {
        if (isBlank(filePath)) {
            throw new RuntimeException("文件或路径不合法,预期不为空");
        }
        return verifyFileDirIfNotExistCreate(new File(filePath));
    }

    /**
     * @param file 文件或者目录
     * @return 返回目录
     */
    public static File verifyFileDirIfNotExistCreate(File file) {
        if (file == null) {
            throw new RuntimeException("文件或路径不合法,预期不为空");
        }
        if (file.isDirectory()) {
            return file;
        } else if (file.isFile()) {
            File parent = file.getParentFile();
            if (parent.exists()) {
                return parent;
            } else {
                //创建目录
                boolean mkdirs = parent.mkdirs();
                if (mkdirs) {
                    return parent;
                } else {
                    log.error("目录创建失败,parent: {}。", parent);
                    throw new RuntimeException("目录创建失败");
                }
            }
        }
        throw new RuntimeException("文件路径推断获取异常");
    }

    public static File createFileIfNotExist(String file) {
        return createFileIfNotExist(new File(file));
    }

    /**
     * 删除单个文件
     *
     * @param file        要删除的文件的文件名
     * @param moveToTrash true=放入回收站 、false物理删除
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(File file, boolean moveToTrash) {

        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (!file.exists()) {
            log.warn("结束返回成功,要删除文件的文件(夹)不存在,file={}", file);
            return true;
        }

        if (moveToTrash) {
            boolean toTrash = TrashUtil.moveToTrash(file);
            log.info("删除文件 toTrash={}:{}", file, toTrash);
            return toTrash;
        }

        if (file.isFile()) {
            boolean delete = file.delete();
            log.info("删除文件{},file:{}", (delete ? "成功" : "失败"), file);
            return delete;
        } else if (file.isDirectory()) {
            boolean delete = deleteDirectory(file);
            log.info("删除文件夹失败{},file:{}", (delete ? "成功" : "失败"), file);
            return false;
        } else {
            log.info("删除单个文件终止-既不是文件也不是文件夹,file:{}", file);
            return false;
        }
    }

    public static boolean deleteFile(List<File> willDeleteFiles, boolean moveToTrash) {
        if (willDeleteFiles == null || willDeleteFiles.isEmpty()) {
            // 被比较的目录的文件hash和基准相同,但是由于位置原因 list为空跳过删除
            return true;
        } else {
            Set<Boolean> delFlag = new HashSet<>();
            for (File willDeleteFile : willDeleteFiles) {
                try {
                    delFlag.add(deleteFile(willDeleteFile, moveToTrash));
                } catch (Exception exception) {
                    log.error("删除文件异常,willDeleteFile:{}", willDeleteFile);

                }
            }

            // 无失败则是成功
            return !delFlag.contains(Boolean.FALSE);

        }
    }
//    public static boolean deleteFile(List<File> willDeleteFiles, boolean moveToTrash) {
//        if (willDeleteFiles == null || willDeleteFiles.isEmpty()) {
//            // 被比较的目录的文件hash和基准相同,但是由于位置原因 list为空跳过删除
//            return true;
//        } else {
//            Set<Boolean> delFlag = new HashSet<>();
//            for (File willDeleteFile : willDeleteFiles) {
//                try {
//                    delFlag.add(deleteFile(willDeleteFile, moveToTrash));
//                } catch (Exception exception) {
//                    log.error("删除文件异常,willDeleteFile:{}", willDeleteFile);
//
//                }
//            }
//
//            // 无失败则是成功
//            return !delFlag.contains(Boolean.FALSE);
//
//        }
//    }


    /**
     * 获取文件名称，支持中文，并自动过滤特殊字符
     * 注意：方法返回支持主流浏览器
     *
     * @param userAgent request.getHeader("User-Agent");
     * @param fileName  文件名称
     * @return 编码过的文件名称
     */
    private static String getFileNameForBrowser(String userAgent, String fileName) {

        if (fileName == null) {
            throw new NullPointerException("SystemFile name cannot be null!");
        }
        fileName = fileName.replaceAll("[^\u4e00-\u9fa5a-zA-Z0-9]", "");
        log.debug("编码前fileName {}", fileName);

        //        String userAgent = request.getHeader("User-Agent");
        log.debug("请求头中的数据-UserAgent{} ", userAgent);

        if (userAgent.indexOf("Firefox") > 0) {//Firefox
            try {
                fileName = new String(fileName.getBytes(StandardCharsets.UTF_8), "ISO8859-1");
            } catch (UnsupportedEncodingException e) {
                log.error("", e);
            }
        } else {//未知
            fileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        }
        log.debug("编码后fileName {}", fileName);
        return fileName;
    }

    public static File getCanWriteFileForce(File file) {
        if (file == null) {
            throw new RuntimeException("文件或路径不合法,预期不为空");
        }

        if (file.isDirectory()) {
            return file;
        } else if (file.isFile()) {
            File parent = file.getParentFile();
            if (parent.exists()) {
                return parent;
            } else {
                //创建目录
                boolean mkdirs = parent.mkdirs();
                if (mkdirs) {
                    return parent;
                } else {
                    log.error("目录创建失败,parent: {}。", parent);
                    throw new RuntimeException("目录创建失败");
                }
            }
        }
        throw new RuntimeException("文件路径推断获取异常");
    }

    /**
     * 删除文件，可以是文件或文件夹
     *
     * @param fileName 要删除的文件名
     * @return true=文件不存在了(删除成功了或者本身就不存在了)，否则返回false
     */
    public static boolean delete(String fileName) {
        if (isBlank(fileName)) {
            log.info("失败,要删除文件的文件不存在,原因文件路径为空");
            return false;
        }
        File file = new File(fileName);
        if (!file.exists()) {
            log.warn("结束返回成功,要删除文件的文件(夹)不存在,fileName={}", fileName);
            return true;
        }

        if (file.isFile() || file.isDirectory()) {
            return deleteFile(file);
        } else {
            throw new RuntimeException("错误,要删除的既不是文件也不是文件夹");
        }

    }

    public static boolean deleteFile(String fileName) {
        return deleteFile(new File(fileName));
    }

    public static boolean deleteFile(File file) {
        log.info("删除文件file={}", file);

        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (!file.exists()) {
            log.warn("结束返回成功,要删除文件的文件(夹)不存在,file={}", file);
            return true;
        }

        if (file.isFile()) {
            boolean delete = file.delete();
            log.info("删除文件{},file:{}", (delete ? "成功" : "失败"), file);
            return delete;
        } else if (file.isDirectory()) {
            boolean delete = deleteDirectory(file);
            log.info("删除文件夹失败{},file:{}", (delete ? "成功" : "失败"), file);
            return false;
        } else {
            log.info("删除单个文件终止-既不是文件也不是文件夹,file:{}", file);
            return false;
        }
    }

    /**
     * 构建目标文件唯一名称-4--通过追加格式化的时间
     *
     * @param sourceFileName 源文件名称
     * @return 唯一目标文件名称
     */
    public static String builtUniqueFilenameOverDate(String sourceFileName, String format) {
        // sourceFileName+时间戳+uuid
        return insertVarToFileName(sourceFileName, EXTENSION_UNDERLINE + getDateString(format));
    }

    public static String builtUniqueFilenameOverDate(String sourceFileName) {
        // sourceFileName+时间戳+uuid
        return insertVarToFileName(sourceFileName, EXTENSION_UNDERLINE + getDateString("yyyyMMddHHMMSSsss"));
    }

    /**
     * 删除目录及目录下的文件
     *
     * @param dir File
     * @return 异常/boolean（true=成功/false=失败）
     */
    public static boolean deleteDirectory(File dir) {
        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dir.exists()) || (!dir.isDirectory())) {
            log.error("失败,原因要删除目录({})不存在", dir);
            return false;
        }

        boolean flag = true;
        // 删除文件夹中的所有文件包括子目录
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                // 删除子文件
                if (file.isFile()) {
                    flag = file.delete();
                } else {
                    deleteDirectory(file);
                }
                log.info("删除{},file:{}", (flag ? "成功" : "失败"), file);
                if (!flag) {
                    String message = String.format("删除文件夹失败,子文件是:%s", file);
                    throw new RuntimeException(message);
                }
            }
        }

        // 删除当前目录
        return dir.delete();
    }

    /**
     * 将inputStream中的文件写入 destination
     *
     * @param inputStream InputStream
     * @param outFile     输出文件
     */
    @Deprecated
    public static void writeToDiskWithTry(InputStream inputStream, File outFile) {
        try {
            writeToDiskCloseable(inputStream, outFile);
        } catch (Exception exception) {
            log.error("Error when write to disk " + outFile, exception);
        }
    }

    public static void writeToDiskCloseable(InputStream inputStream, File outFile) throws IOException {

        FileUtil.requireFileExistsOrElseTryCreate(outFile);

        try (FileOutputStream fileOutputStream = new FileOutputStream(outFile); BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream); BufferedInputStream bis = new BufferedInputStream(inputStream)) {
            int count, bufferSize = 4096;
            byte[] buffer = new byte[bufferSize];
            while ((count = bis.read(buffer, 0, bufferSize)) != -1) {
                bos.write(buffer, 0, count);
            }
        } finally {
            inputStream.close();
        }
        log.info("文件保存在本地磁盘的位置为={}", outFile);

    }

    /**
     * 获取指定目录下的全部文件
     * 当前文件夹以及子文件夹不包含在返回值的list中
     * 会包含 .DS_Store
     *
     * @param directory 支持传入文件 或者文件路径
     * @return list
     */
    public static List<File> listFileByDir(File directory) {
        //log.debug("指定目录下的全部文件,目录={}", directory);
        if (directory == null) {
            return Collections.emptyList();
        }

        List<File> files = new ArrayList<>();

        // 是一个文件
        if (directory.isFile()) {
            //files.add(directory);            return files;
            directory = directory.getParentFile();
        }
        //log.info("要处理的文件最上层父路径是={}", directory.getPath());

        //是一个目录
        if (directory.isDirectory()) {
            File[] tempFiles = directory.listFiles();
            if (tempFiles != null && tempFiles.length >= 1) {
                for (File tempFile : tempFiles) {
                    if (tempFile.isDirectory()) {
                        files.addAll(listFileByDir(tempFile));
                    } else {
                        files.add(tempFile);
                    }
                }
            }

        }
        return files;
    }

    public static List<File> listFileByDir(String directoryPath) {
        if (isBlank(directoryPath)) {
            return Collections.emptyList();
        }
        return listFileByDir(new File(directoryPath));
    }

    /**
     * 忽略指定名称的条件下列出目录下的全部文件
     *
     * @param directoryPath 文件目录
     * @param fileFilter    是指定名称数据字典
     * @return List
     */
    public static List<File> listFileByDir(String directoryPath, Collection<String> fileFilter) {
        if (isBlank(directoryPath)) {
            return null;
        }
        List<File> files = listFileByDir(new File(directoryPath));
        if (fileFilter == null || fileFilter.isEmpty()) return files;

        return files.stream().filter(it -> it != null && !fileFilter.contains(it.getName())).collect(Collectors.toList());
    }

    private static void verifyFileNotNull(File targetFile, String message) {
        if (targetFile == null) {
            throw new RuntimeException(message);
        }
    }
    /*
     * 目标路文件的几种情况
     * 1 目标路文件夹不存在
     * 1.1 --新建目标文件夹成功--移动
     * 1.2 --新建文件夹失败--抛出异常
     * 2 目标路文件夹存在且是空的--移动
     * 3 目标路文件夹存在且是非空
     * 3.1 --没有同名文件--移动
     * 3.2 --有同名称文件--若指定强制覆盖则替换 否则,同名文件后追加唯一标识后移动
     *
     * 抛出异常的情况:
     * 1 移动时候如果磁盘空间不足
     * 2 目标路径不存在且尝试创建时失败
     * 3 文件名称非法(系统不支持:例如特殊符号、过长等)
     * 4 读写权限问题
     * 5 异常终止(磁盘丢失、断电等)
     */


    /**
     * 移动指定文件夹下的全部文件到指定目录 或者移动指定文件到指定目录
     *
     * @param sourceFile     源文件/源文件文件夹
     * @param targetFile     可以是文件也可以是目录
     * @param ifSameAction   如果重复动作是：什么都不做/覆盖/不覆盖  -1=不要移动、0=覆盖，1=重命名后尽量移动
     * @param ignoreEmptyDir 是否移动空的源文件夹 true= 不移动空文件夹 false=移动空文件夹
     * @return false=移动失败 其他=成功=没有消息就是最好的消息思想
     */
    @Deprecated
    public static String moveFile(File sourceFile, File targetFile, int ifSameAction, Boolean ignoreEmptyDir) {
        // 目标读写性判断
        if (enableDebug) {
            log.info("文件移动,,\nsourceFile={}\ntargetFile={}\ntargetFile.exists()={}\ntargetFile.canWrite()={}", sourceFile, targetFile, targetFile.exists(), targetFile.canWrite());
        }

        try {
            requireFileExistsOrElseThrows(sourceFile);
            if (targetFile == null) {
                throw new RuntimeException("错误,目标文件为null");
            }

            if (targetFile.isFile()) {
                if (sourceFile.isFile()) {
                    // 文件-->文件
                    verifyFileNotNull(sourceFile, "文件移动错误,文件(夹)不存在");
                    verifyFileNotNull(targetFile, "文件移动错误,文件(夹)不存在");

                    renameTo(sourceFile, targetFile, ifSameAction);

                } else if (sourceFile.isDirectory()) {
                    // 文件夹-->文件
                    verifyFileNotNull(sourceFile, "文件移动错误,文件(夹)不存在");
                    verifyFileNotNull(targetFile, "文件移动错误,文件(夹)不存在");
                    throw new RuntimeException("文件移动错误,原因:文件夹-->文件");

                    //moveSourceFileDirToTargetFileDir(sourceFile, targetFile.getParentFile());
                }

            } else if (targetFile.isDirectory()) {
                if (sourceFile.isFile()) {
                    // 文件-->文件夹
                    verifyFileNotNull(sourceFile, "文件移动错误,文件(夹)不存在");
                    verifyFileNotNull(targetFile, "文件移动错误,文件(夹)不存在");

                    renameTo(sourceFile, targetFile, ifSameAction);

                } else if (sourceFile.isDirectory()) {
                    // 文件夹-->文件夹
                    moveSourceFileDirToTargetFileDir(sourceFile, targetFile);
                }
            } else {
                throw new RuntimeException("文件移动错误,目标既不是文件也不是文件夹");
            }


        } catch (Exception exception) {
            log.error("error when ", exception);
            return "ERROR " + exception;
        }

        return "OK";
    }

    // 文件夹-->文件夹
    public static void moveSourceFileDirToTargetFileDir(File sourceFile, File targetFile) {
        verifyFileNotNull(sourceFile, "文件移动错误,sourceFile文件(夹)不存在");
        verifyFileNotNull(targetFile, "文件移动错误,targetFile文件(夹)不存在");

        if (!targetFile.exists()) {
            verifyFileDirIfNotExistCreate(targetFile);
        }
        try {
            log.info("移动文件【文件夹-->文件夹】,源文件夹删除-成功");
            final File targetDir = new File(targetFile.getAbsolutePath() + File.separator + sourceFile.getName());
            if (!targetDir.exists()) {
                final boolean mkdirs = targetDir.mkdirs();
                if (mkdirs) {
                    log.info("移动文件【文件夹-->文件夹】,目标文件夹创建成功");
                    if (sourceFile.delete()) {
                        log.info("移动文件【文件夹-->文件夹】,源文件夹删除-成功");
                    } else {
                        log.info("移动文件【文件夹-->文件夹】,源文件夹删除-失败");
                    }

                } else {
                    log.info("移动文件【文件夹-->文件夹】,源文件夹删除-成功 & 目标文件夹创建成功");
                }
            }


        } catch (Exception exception) {
            log.error("error when 源文件夹删除", exception);
        }

    }

    /**
     * 非线程安全
     *
     * @param sourceFile   原始文件
     * @param outputFile   目标文件/文件夹
     * @param ifSameAction 移动后文件冲突，可能的操作：-1=不移动、0=覆盖，1=重命名后尽量移动
     */
    public static void renameTo(final File sourceFile, File outputFile, int ifSameAction) {
        log.info("移动文件-renameTo,sourceFile={},targetFile={}", sourceFile, outputFile);

        if (sourceFile == null) {
            throw new RuntimeException("文件移动错误,源文件(夹)不存在");
        }

        if (outputFile == null) {
            throw new RuntimeException("文件移动错误,目标文件/文件夹不存在");
        }
        log.debug("源文件 isFile={},存在={},source={}", sourceFile.isFile(), sourceFile.exists(), sourceFile);
        log.debug("目标文件 isFile={},存在={},outputFile={}", outputFile.isFile(), outputFile.exists(), outputFile);


        // !!! 特殊文件直接删除 “DS_Store”
        if (sourceFile.getName().contains("DS_Store") || sourceFile.getName().contains("Thumbs.db")) {
            System.out.println("exists=" + sourceFile.exists());
            System.out.println("isFile=" + sourceFile.isFile());
            System.out.println("isDirectory=" + sourceFile.isDirectory());
            System.out.println("canWrite=" + sourceFile.canWrite());
            System.out.println("length=" + sourceFile.length());
            System.out.println("length=" + sourceFile.isHidden());
            boolean delete = sourceFile.delete();
            System.out.println("\033[35;4m" + "删除特殊文件:delete=" + (delete ? "成功" : "失败") + "\n" + sourceFile + "\n\033[0m");
            return;
        }

        if (!sourceFile.isFile()) {
            throw new RuntimeException("文件移动错误,源文件不是一个文件source=" + sourceFile);
        }

        if (outputFile.exists()) {
            switch (ifSameAction) {// 可能的操作：-1=不移动、0=覆盖，1=重命名后尽量移动
                case -1:
                    log.info("移动文件，目标文件已经存在,不移动(-1)");
                    return;
                case 0:
                    boolean delete = deleteFile(outputFile, true);
                    if (!delete) {
                        log.error("文件移动失败,原因：覆盖模式(0)先删除后创建目标文件,目标文件已经存在，删除目标文件失败");
                        return;
                    } else {
                        renameToFileToFileOnly(sourceFile, outputFile);
                    }
                    return;

                case 1:
                    File renamedOutputFile = detectionTargetFileV2(sourceFile.getName(), outputFile, "");
                    renameToFileToFileOnly(sourceFile, renamedOutputFile);

                    return;

                default:
                    log.info("移动文件与目标文件冲突，可能的操作ifSameAction：-1=不要移动、0=覆盖，1=重命名后尽量移动");
            }

        } else {
            File renamedOutputFile = detectionTargetFileV2(sourceFile.getName(), outputFile, "");
            renameToFileToFileOnly(sourceFile, renamedOutputFile);

        }


    }

    public static void renameToFileToFileOnly(File sourceFile, File targetFile) {

        // 移动
        boolean renameTo = sourceFile.renameTo(targetFile);
        if (enableDebug)
            log.info("renameToFileToFileOnly文件移动renameTo结果={},源-->目标\n{}\n{}\n", (renameTo ? "成功" : "失败"), sourceFile, targetFile);

        // 移动失败重试  手动拷贝文件 然后删除源文件
        if (!renameTo) {
            log.warn("文件移动成功-失败-将重试");
            try {
                writeToDiskWithTry(new FileInputStream(sourceFile), targetFile);
                deleteFile(sourceFile, true);
                if (enableDebug)
                    log.info("文件移动(重试方式)-成功,{}-->\ntargetFile.getPath():\n{}\ntoDiskPath:\n{}", sourceFile.getPath(), targetFile.getPath(), targetFile);

            } catch (FileNotFoundException exception) {
                log.error("Error 文件移动(重试方式)-失败", exception);

            }
        }


    }

    public static String insertVarToFileName(String sourceFileName, Object appendVar) {
        return insertVarToFileName(sourceFileName, appendVar, false);

    }

    public static String insertVarToFileName(String sourceFileName, Object insertVar, boolean insertAtStart) {
        FileName fileName = analysisFileName(sourceFileName);
        log.info("文件fileName={}", JSON.toJSONString(fileName));
        log.info("文件插入片段var={}", insertVar);
        final String tempContactFileName;
        if (insertAtStart) {
            tempContactFileName = insertVar + fileName.getPrefix() + fileName.getSuffix();
        } else {
            tempContactFileName = fileName.getPrefix() + insertVar + fileName.getSuffix();
        }
        return tempContactFileName;

    }

    public static File detectionTargetFileV2(File sourceFile, File outputFolder, String insertVar) {
        return detectionTargetFileV2(sourceFile.getName(), outputFolder, insertVar);
    }

    public static File detectionTargetFileV2(String sourceFileName, File outputFileDir, String prefixAdd) {
        if (outputFileDir == null) {
            throw new IllegalArgumentException("输出目录不是一个有效的目录或文件。");
        }

        if (outputFileDir.isFile()) {
            outputFileDir = outputFileDir.getParentFile();
        }
        if (!outputFileDir.isDirectory()) {
            throw new IllegalArgumentException("输出目录不是一个有效的目录。");
        }
        if (prefixAdd == null) {
            prefixAdd = "";
        }

        final FileName fileName = analysisFileName(sourceFileName);

        String baseName = fileName.getPrefix();
        String extension = fileName.getSuffix();
        String targetFileName = prefixAdd + sourceFileName;

        int counter = 1;
        while (new File(outputFileDir, targetFileName).exists()) {
            targetFileName = prefixAdd + baseName + "_" + counter + extension;
            counter++;
        }

        return new File(outputFileDir, targetFileName);
    }

    // 相同文件不在重复处理 prefix 字首  suffix 字尾
    public static File detectionTargetFileV3(File outputFileDir, String sourceFileName, String suffixAdd) {
        if (outputFileDir == null) {
            String error = "输出目录不是一个有效的目录";
            log.error(error);
            throw new IllegalArgumentException(error);
        }

        if (outputFileDir.isFile()) {
            outputFileDir = outputFileDir.getParentFile();
        }
        if (!outputFileDir.isDirectory()) {
            throw new IllegalArgumentException("输出目录不是一个有效的目录。");
        }

        final FileName fileName = analysisFileName(sourceFileName);

        String baseName = fileName.getPrefix();
        String extension = fileName.getSuffix();
        String targetFileName = sourceFileName + suffixAdd;

        int counter = 1;
        while (new File(outputFileDir, targetFileName).exists()) {
            targetFileName = baseName + suffixAdd + "_" + counter + extension;
            counter++;
        }

        return new File(outputFileDir, targetFileName);
    }

    public static void writeToDisk(String contents, String outputFile, boolean override) {
        writeToDisk(Collections.singletonList(contents), outputFile, override);
    }

    public static void writeToDisk(String contents, String outputFile) {
        writeToDisk(Collections.singletonList(contents), outputFile, false);
    }

    public static void writeToDisk(Collection<String> contents, File outputFile, boolean override) {
        writeToDisk(contents, outputFile.getPath(), override);
    }

    /**
     * 方法可能抛出异常
     *
     * @param contents   要写出到文件的文本
     * @param outputFile 输出文件的绝对路径
     * @param override   是否覆盖写 true=覆盖写, false=追加写
     */
    @Deprecated
    public static void writeToDisk(Collection<String> contents, String outputFile, boolean override) {
        if (outputFile == null) {
            outputFile = "./" + getDateString("yyyyMMddHHMMSSsss") + "-output.txt";
        }

        File outFile = new File(outputFile);
        FileUtil.requireFileDirectoryExistsOrElseTryCreate(outFile.getParentFile());

        FileUtil.requireFileExistsOrElseTryCreate(outFile);

        if (!outFile.canWrite()) {
            throw new RuntimeException("目标文件无法写入");
        }

        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            fileWriter = new FileWriter(outFile, true);
            //使用缓冲区比不使用缓冲区效果更好，因为每趟磁盘操作都比内存操作要花费更多时间。
            //通过BufferedWriter和FileWriter的连接，BufferedWriter可以暂存一堆数据，然后到满时再实际写入磁盘
            //这样就可以减少对磁盘操作的次数。如果想要强制把缓冲区立即写入,只要调用writer.flush();这个方法就可以要求缓冲区马上把内容写下去
            bufferedWriter = new BufferedWriter(fileWriter);
            int count = 0;
            for (String content : contents) {
                count += 1;
                bufferedWriter.write(content + "\n");
                if (count == 10) {
                    count = 0;
                    //log.info("数据写入磁盘...");
                    bufferedWriter.flush();
                }
            }

            bufferedWriter.flush();

        } catch (IOException exception) {
            log.error("写文件异常", exception);
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.flush();
                    fileWriter.close();
                } catch (IOException e) {
                    log.error("close exception", e);
                }
            }
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    log.error("close exception", e);

                }
            }
        }
    }

    /**
     * 小数计算（四舍五入）：解决DecimalFormat("#.00")使用时小数点后第三位值为5，第二位为偶数时无法进位的问题
     */
    public static double decimalCalculation(Double startVal) {
        DecimalFormat df = new DecimalFormat("#.00");
        String startStr = startVal.toString();
        String startDecimal = startStr.split("\\.")[1];
        double temp = 0.01;
        double endVal;
        if (startDecimal.length() > 2 && "5".equals(String.valueOf(startDecimal.charAt(2))) && Integer.parseInt(String.valueOf(startDecimal.charAt(1))) % 2 == 0) {
            endVal = Double.parseDouble(startStr.substring(0, startStr.length() - 1)) + temp;
        } else {
            endVal = Double.parseDouble(df.format(startVal));
        }
        return Double.parseDouble(df.format(endVal));
    }


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

    public static void open(String fullFileName) {
        open(new File(fullFileName));

    }

    public static void open(File file) {
        if (file == null || !file.exists()) {
            log.warn("File does not exist: " + file);
            return;
        }
        if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
            try {
                desktop.open(file);
            } catch (IOException e) {
                log.error("打开文件异常", e);
            }
        } else {
            String os = System.getProperty("os.name").toLowerCase();
            try {
                if (os.contains("win")) {
                    Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "\"\"", file.getAbsolutePath()});
                } else if (os.contains("mac")) {
                    Runtime.getRuntime().exec(new String[]{"open", file.getAbsolutePath()});
                } else if (os.contains("nux") || os.contains("nix")) {
                    Runtime.getRuntime().exec(new String[]{"xdg-open", file.getAbsolutePath()});
                } else {
                    log.warn("Cannot open file on this OS in headless mode: " + file);
                }
            } catch (IOException e) {
                log.error("Fallback open failed for file: " + file, e);
            }
        }

    }


    /// ***************** 文件名称处理 START *****************


    /// ***************** 文件名称处理 END *****************


    /// ***************** 文件大小 START *****************
    public static long sizeOf(final String file) {
        return sizeOf(new File(file));
    }

    public static long sizeOf(final File file) {
        requireExists(file, "file");
        return file.isDirectory() ? sizeOfDirectory0(file) : file.length();
    }

    public static File requireExists(final String path) {
        if (isBlank(path)) return null;

        File file = new File(path);
        if (file.exists()) {
            return file;
        } else {
            return null;
        }

    }

    @Deprecated
    public static File requireExistsOrElseThrows(final String path) {
        if (isBlank(path)) throw new RuntimeException("路径不能为空");

        File file = new File(path);
        if (file.exists()) {
            return file;
        } else {
            throw new RuntimeException("文件不存在");
        }

    }

    /**
     * 校验文件必须存在否则抛出异常
     *
     * @param file 待检测文件
     * @return 通过检测的文件 or throws Exception
     */
    public static File requireFileExistsOrElseThrows(final File file) {
        if (file == null) throw new RuntimeException("file must not null!(文件不可为null)");
        if (!file.exists()) throw new RuntimeException("file must exist!(文件必须存在)");
        if (!file.isFile()) throw new RuntimeException("file must is file!(文件必须是文件)");
        return file;
    }

    /**
     * 验证文件存在 否则尝试创建文件
     * 注意： 本方法会验证父级路径验是否存在,不存在则创建父级路径
     * 任何一步失败则抛出异常
     *
     * @param file 文件（绝对路径）
     */
    public static void requireFileExistsOrElseTryCreate(final File file) {
        if (file == null) throw new RuntimeException("file must not null!(文件不可为null)");
        if (file.exists()) {
            return;
        }

        // 父级路径验证存在否则创建
        requireFileDirectoryExistsOrElseTryCreate(file.getParentFile());
        boolean createNewFile;
        try {
            createNewFile = file.createNewFile();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        if (createNewFile) {
            log.warn("Create new file success!({}) ", file);
        } else {
            String error = "Create new file failure!";
            log.error(error + "({}) ", file);
            throw new RuntimeException(error);
        }
    }

    /**
     * 校验文件夹必须存在否则抛出异常
     *
     * @param file 待校验文件夹
     * @return 通过检测的文件夹 or throws Exception
     */
    public static void requireFileDirectoryExistsOrElseThrows(final File file) {
        if (file == null) throw new RuntimeException("file must not null! 文件夹不可为null");
        if (!file.exists()) throw new RuntimeException(String.format("file must exist!文件夹必须存在(%s)", file));
        if (!file.isDirectory())
            throw new RuntimeException(String.format("file must is file!文件必须是文件夹(%s)", file));
    }

    public static File requireFileDirectoryExistsOrElseTryCreate(final File file) {
        if (file == null) throw new RuntimeException("file must not null!(文件夹不可为null)");
        if (file.exists()) {
            if (file.isDirectory()) {
                return file;
                //throw new RuntimeException("file must is file!(文件必须是文件夹)");
            } else {
                log.warn("file exist but not is directory & will try create!(文件存在，但是不是文件夹,尝试创建！)");
                return file.getParentFile();
            }

        } else {
            boolean mkdirs = file.mkdirs();
            if (mkdirs) {
                log.warn("Create file directory success!({}) ", file);
                return file;
            } else {
                log.error("Create file directory error! file=" + file);
                throw new RuntimeException("file directory not exist and create failure!(文件夹不存在，且创建失败)");
            }
        }
    }

    /**
     * Requires that the given {@code File} exists and throws an {@link IllegalArgumentException} if it doesn't.
     *
     * @param file          The {@code File} to check.
     * @param fileParamName The parameter name to use in the exception message in case of {@code null} input.
     * @return the given file.
     * @throws NullPointerException     if the given {@code File} is {@code null}.
     * @throws IllegalArgumentException if the given {@code File} does not exist.
     */
    public static File requireExists(final File file, final String fileParamName) {
        Objects.requireNonNull(file, fileParamName);
        if (!file.exists()) {
            throw new IllegalArgumentException("File system element for parameter '" + fileParamName + "' does not exist: '" + file + "'");
        }
        return file;
    }

    public static File requireExistsAndIsFile(final File file, final String fileParamName) {
        Objects.requireNonNull(file, fileParamName);
        if (!file.exists()) {
            throw new IllegalArgumentException("File system element for parameter '" + fileParamName + "' does not exist: '" + file + "'");
        }
        if (file.isFile()) {
            return file;
        }
        throw new RuntimeException(fileParamName + "is not a file ");
    }

    /**
     * Gets the size of a directory.
     *
     * @param directory the directory to check
     * @return the size
     * @throws NullPointerException if the directory is {@code null}.
     */
    private static long sizeOfDirectory0(final File directory) {
        Objects.requireNonNull(directory, "directory");
        final File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            return 0L;
        }
        long size = 0;

        for (final File file : files) {
            if (!isSymlink(file)) {
                size += sizeOf0(file);
                if (size < 0) {
                    break;
                }
            }
        }

        return size;
    }

    private static long sizeOf0(final File file) {
        Objects.requireNonNull(file, "file");
        if (file.isDirectory()) {
            return sizeOfDirectory0(file);
        }
        return file.length(); // will be 0 if file does not exist
    }

    public static boolean isSymlink(final File file) {
        return file != null && Files.isSymbolicLink(file.toPath());
    }

    /// ***************** 文件大小 END *****************


    public static TreeSet<String> readLines(File source) {
        BufferedReader reader = null;
        String temp = null;
        TreeSet<String> lines = new TreeSet<>();
        int line = 1;
        try {
            reader = new BufferedReader(new FileReader(source));
            while ((temp = reader.readLine()) != null) {
                lines.add(temp);
                line++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return lines;
    }


    public static String readLinesAsString(File source) {
        return readLinesAsString(source, false);
    }

    /**
     * @param filePath            file path
     * @param showDebugLineNumber debug
     * @return content  / null(if occur error)
     */
    public static String readLinesAsString(Path filePath, boolean showDebugLineNumber) {
        return FileReadTool.readLinesAsString(filePath, showDebugLineNumber);
    }

    public static String readLinesAsString(File file, boolean showDebugLineNumber) {
        return FileReadTool.readLinesAsString(file.toPath(), showDebugLineNumber);
    }

    //***************************************************
    //                   工具方法2- 文件名称处理
    //***************************************************

    public static String getFileType(String fullFileName) {
        if (isBlank(fullFileName) || !fullFileName.contains(".") || isBlank(FileUtil.getExtension(fullFileName))) {
            //throw new RuntimeException("错误,预期参数(name)不为空");
            return FileUtilConst.FILE;
        } else {
            String extension = FileUtil.getExtension(fullFileName).toUpperCase(Locale.ROOT);
            if (isSuffixesEnd(audioSuffixes, extension)) {
                return AUDIO;
            } else if (isSuffixesEnd(videoSuffixes, extension)) {
                return VIDEO;
            } else if (isSuffixesEnd(imageSuffixes, extension)) {
                return IMAGE;
            } else {
                return FileUtilConst.FILE;
            }
        }

    }

    public static boolean isSuffixesEnd(Set<String> suffixes, String filenameSuffix) {
        if (suffixes == null || suffixes.isEmpty()) throw new RuntimeException("Set<String>suffixes not allow blank");
        if ((filenameSuffix == null) || (filenameSuffix.length() < 1)) {
            return false;
        }
        //判断
        return suffixes.contains(filenameSuffix.toUpperCase(Locale.ROOT));
    }

    @Deprecated
    public static boolean isImage(String filename) {
        String extension = getExtension(filename);
        return isSuffixesEnd(imageSuffixes, extension);
    }

    public static boolean isImage(File file) {
        if (!file.isFile()) return false;
        String extension = getExtension(file.getName());
        return isSuffixesEnd(imageSuffixes, extension);

    }

    public static boolean isVideo(File file) {
        if (!file.isFile()) return false;
        String extension = getExtension(file.getName());
        return isSuffixesEnd(videoSuffixes, extension);
    }

    public static boolean isAudio(File file) {
        if (!file.isFile()) return false;
        return isSuffixesEnd(audioSuffixes, getExtension(file).toUpperCase());
    }

    public static boolean isAudio(String fileExtension) {
        if (isBlank(fileExtension)) return false;
        return isSuffixesEnd(audioSuffixes, fileExtension.toUpperCase());
    }

    public static boolean isTxt(File file) {
        if (!file.isFile()) return false;
        String extension = getExtension(file.getName());
        return isSuffixesEnd(textSuffixes, extension);
    }

    public static boolean isOffice(File file) {
        if (!file.isFile()) return false;
        String extension = getExtension(file.getName());
        return isSuffixesEnd(officeSuffixes, extension);
    }

    public static boolean isBlank(String content) {
        return content == null || content.trim().isEmpty();
    }

    /**
     * 获取现在时间字符串
     */
    public static String getDateString(String format) {
        return new SimpleDateFormat(format).format(new Date());
    }

//    /**
//     * 获取uuid变体，去除了“-”
//     *
//     * @return 获取uuid变体
//     */
//    public static String getUUID() {
//        return UUID.randomUUID().toString().replace("-", "");
//    }

//
//    /**
//     * 构建目标文件唯一名称-2--通过追加时UUID方式
//     *
//     * @param sourceFileName 源文件名称
//     * @return 唯一目标文件名称
//     */
//    public static String builtUniqueFilenameOverUUID(String sourceFileName) {
//        // sourceFileName+uuid
//        return insertVarToFileName(sourceFileName, EXTENSION_UNDERLINE + getUUID());
//    }
//
//    /**
//     * 构建目标文件唯一名称-2--通过追加时间戳方式
//     *
//     * @param sourceFileName 源文件名称
//     * @return 唯一目标文件名称
//     */
//    public static String builtUniqueFilenameOverTimestamp(String sourceFileName) {
//        // sourceFileName+时间戳
//        return insertVarToFileName(sourceFileName, EXTENSION_UNDERLINE + System.currentTimeMillis());
//    }
//
//    /**
//     * 构建目标文件唯一名称-3--通过追加时间戳+UUID方式
//     *
//     * @param sourceFileName 源文件名称
//     * @return 唯一目标文件名称
//     */
//    public static String builtUniqueFilenameOverTimestampAndUUID(String sourceFileName) {
//        // sourceFileName+时间戳+uuid
//        return insertVarToFileName(sourceFileName, EXTENSION_UNDERLINE + System.currentTimeMillis() + getUUID());
//    }

    /**
     * 提取文件名
     *
     * @param originalFileName 原始文件名称
     * @return 文件名
     */
    public static String extractFileName(String originalFileName) {
        if (isBlank(originalFileName) || !originalFileName.contains(".")) {
            return originalFileName;
        }
        return originalFileName.substring(0, originalFileName.lastIndexOf("."));
    }

    public static String extractFileName(File file) {
        if (file == null) return null;
        return extractFileName(file.getName());
    }

    /**
     * 获取分隔符的index
     *
     * @param filename 文件名称
     * @return 索引
     */
    @Deprecated
    public static int getIndexOfLastSeparator(final String filename) {
        if (filename == null) {
            return NOT_FOUND;
        }
        final int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
        final int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
        return Math.max(lastUnixPos, lastWindowsPos);
    }

    /**
     * 获取扩展名的index
     *
     * @param filename 文件名称
     * @return 扩展名的index
     */
    @Deprecated
    public static int getIndexOfExtension(final String filename) {
        if (filename == null) {
            return NOT_FOUND;
        }

        // EXTENSION_SEPARATOR=. UNIX_SEPARATOR=/ WINDOWS_SEPARATOR=\\
        final int extensionPos = filename.lastIndexOf(".");//EXTENSION_SEPARATOR=.
        final int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
        final int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
        final int lastSeparator = Math.max(lastUnixPos, lastWindowsPos);

        return lastSeparator > extensionPos ? NOT_FOUND : extensionPos;
    }

    /**
     * 删除文件扩展名
     *
     * @param filename 文件名称
     * @return String
     */
    public static String deleteExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    public static String getExtension(File file) {
        return getExtension(requireFileExistsOrElseThrows(file).getName());
    }

    public static boolean isExtensionEndWith(File file, List<String> allowFileTypes) {
        final String extension = FileUtil.getExtensionWithTry(file, "").toLowerCase(Locale.ROOT);
        return (allowFileTypes.contains(extension));
    }

    public static String getExtensionWithTry(File file, String defaultResponse) {
        try {
            return getExtension(file);
        } catch (Exception exception) {
            log.error("Error when ", exception);
            return defaultResponse;
        }
    }

    /**
     * 获取文件扩展名 /提取文件名后缀
     *
     * @param filename 文件名称
     * @return 扩展名称(没有那个点)
     */
    public static String getExtension(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return "";
    }


    public static String getNameWithExtension(String path) {
        if ((path != null) && (path.length() > 0)) {

            String flag = File.separator;
            if (!path.contains(flag)) {
                flag = "%2F";
            }

            int dot = path.lastIndexOf(flag);
            if (dot == -1) {
                return "";
            }

            return path.substring(dot + flag.length());
        }
        return "";
    }


    /**
     * 获取文件扩展名/后缀
     *
     * @param filename 文件名称
     * @return 文件扩展名
     */
    public static String getExtensionV0(final String filename) {
        if (filename == null) {
            return null;
        }
        final int index = getIndexOfExtension(filename);
        if (index == NOT_FOUND) {
            return "";
        } else {
            return filename.substring(index + 1);
        }
    }


    public static FileName analysisFileName(String originalFileName) {

        if (isBlank(originalFileName) || !originalFileName.contains(".")) {
            return new FileName(originalFileName, originalFileName, "");
        }
        int indexOfLastPoint = originalFileName.lastIndexOf(".");
        String prefix = originalFileName.substring(0, indexOfLastPoint);
        String suffix = originalFileName.substring(indexOfLastPoint);
        return new FileName(originalFileName, prefix, suffix);
    }

    /**
     * @param originalFileName 文件名
     * @return 非null数组，第一个是名称 第二个参数是扩展名
     */
    public static String[] analysisFileNameAndExtension(String originalFileName) {

        if (originalFileName == null) {
            return new String[]{null, null};

        }
        if (isBlank(originalFileName) || !originalFileName.contains(".")) {
            return new String[]{originalFileName, ""};
        }

        int indexOfLastPoint = originalFileName.lastIndexOf(".");
        String prefix = originalFileName.substring(0, indexOfLastPoint);
        String suffix = originalFileName.substring(indexOfLastPoint);
        return new String[]{prefix, suffix};
    }

    /**
     * 获取文件扩展名，不带 .
     */
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return "";
    }

    /**
     * Java文件操作 获取不带扩展名的文件名
     */
    public static String getFilenameWithoutExtension(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    public static int getFilenameWithoutExtensionIndex(final String filename) {
        if (filename == null) {
            return NOT_FOUND;
        }
        final int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
        final int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
        return Math.max(lastUnixPos, lastWindowsPos);
    }

    private static void fun1() {
        System.out.println("24534612203910144".length());
        System.out.println(new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));//20190113 180709850

        System.out.println();
        String uuid = UUID.randomUUID().toString();
        String uuidReplace = uuid.replace("-", "");
        System.out.println(uuid);                      //c4bf200f-85bf-431c-b91a-1c4c8247f1d7
        System.out.println(uuidReplace);        //c4bf200f85bf431cb91a1c4c8247f1d7
        System.out.println();
        Object x = Math.random();
        System.out.println("random--> " + x);
        String file2 = "a/b/c/application.txt";
        System.out.println(getIndexOfLastSeparator("application.txt"));
        System.out.println(getIndexOfLastSeparator(file2));
        System.out.println();
        System.out.println(getExtension(file2));
    }

    public static boolean deleteDirectoryV0(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录的
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDirectory(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

    public static void main(String[] args) {
//        File inputFile = new File("H:\\code\\java\\fx\\src\\main\\java\\com\\arc\\fx\\tool\\adb\\adb\\SONY手机常见软件包名称.txt");
//        File outputFile = new File("H:\\code\\java\\fx\\src\\main\\java\\com\\arc\\fx\\tool\\adb\\adb\\SONY手机常见软件包名称-no-duplicated.txt");

        File inputFile = new File("H:\\code\\java\\fx\\src\\main\\java\\com\\arc\\fx\\tool\\adb\\s62\\all.txt");
        File outputFile = new File("H:\\code\\java\\fx\\src\\main\\java\\com\\arc\\fx\\tool\\adb\\s62\\all-no-duplicated.txt");

        TreeSet<String> lines = FileUtil.readLines(inputFile);
        FileUtil.writeToDisk(lines, outputFile, true);
    }


//    public static boolean deleteFile(List<File> willDeleteFiles, boolean moveToTrash) {
//        if (willDeleteFiles == null || willDeleteFiles.isEmpty()) {
//            // 被比较的目录的文件hash和基准相同,但是由于位置原因 list为空跳过删除
//            return true;
//        } else {
//            Set<Boolean> delFlag = new HashSet<>();
//            for (File willDeleteFile : willDeleteFiles) {
//                try {
//                    delFlag.add(deleteFile(willDeleteFile, moveToTrash));
//                } catch (Exception exception) {
//                    log.error("删除文件异常,willDeleteFile:{}", willDeleteFile);
//
//                }
//            }
//
//            // 无失败则是成功
//            return !delFlag.contains(Boolean.FALSE);
//
//        }
//    }

    /**
     * 删除
     *
     * @param files files
     */
    private void deleteFile(File... files) {
        for (File file : files) {
            if (file.exists()) {
                try {
                    boolean delete = file.delete();
                } catch (Exception e) {
                    log.error("ERROR 文件删除时候出错={}，filePath={}", e, file.getPath());
                }
            }
        }
    }

    public static class FileUtilConst {

        /**
         * 英文点号
         * The extension separator character.
         *
         * @since 1.4
         */
        public static final char EXTENSION_SEPARATOR = '.';
        /**
         * 正斜线
         * The Unix separator character.
         */
        public static final String UNIX_SEPARATOR = "/";
        /**
         * 反斜线
         * The Windows separator character.
         */
        public static final String WINDOWS_SEPARATOR = "\\\\";
        /**
         * 下划线;
         */
        public static final char EXTENSION_UNDERLINE = '_';
        /**
         * 减号
         */
        public static final String MINUS_SIGN = "-";
        /**
         * 找不到，坐标溢出
         */
        public static final int NOT_FOUND = -1;
        public static final String VIDEO = "VIDEO";
        public static final String AUDIO = "AUDIO";
        public static final String IMAGE = "IMAGE";
        public static final String FILE = "FILE";
        /**
         * 图片类型文件扩展名
         */
        public static Set<String> imageSuffixes = new HashSet<>();
        /**
         * 音频类型文件扩展名
         */
        public static Set<String> audioSuffixes = new HashSet<>();
        public static Set<String> videoSuffixes = new HashSet<>();

        public static Set<String> textSuffixes = new HashSet<>();
        public static Set<String> officeSuffixes = new HashSet<>();
        public static Set<String> zipSuffixes = new HashSet<>();


        //***************************************************
        //                   工具方法- 封装方法
        //***************************************************
        static DecimalFormat decimalFormat = new DecimalFormat("#.00");

        static {

            //BMP格式、PCX格式、TIF格式、GIF格式、JPEG格式、TGA格式、EXIF格式。
            imageSuffixes.add("BMP");
            imageSuffixes.add("PCX");
            imageSuffixes.add("TIF");
            imageSuffixes.add("GIF");
            imageSuffixes.add("JPEG");
            imageSuffixes.add("TGA");
            imageSuffixes.add("EXIF");
            imageSuffixes.add("JFIF");
            imageSuffixes.add("JPG");
            imageSuffixes.add("ORF");


            //FPX格式、SVG格式、PSD格式、CDR格式、PCD格式、DXF格式、UFO格式、EPS格式。
            imageSuffixes.add("SVG");
            imageSuffixes.add("PSD");
            imageSuffixes.add("CDR");
            imageSuffixes.add("PCD");
            imageSuffixes.add("DXF");
            imageSuffixes.add("UFO");
            imageSuffixes.add("EPS");

            //AI格式、PNG格式、HDRI格式、RAW格式、WMF格式、FLIC格式、EMF格式、ICO格式。
            imageSuffixes.add("PNG");
            imageSuffixes.add("HDRI");
            imageSuffixes.add("RAW");
            imageSuffixes.add("WMF");
            imageSuffixes.add("WEBP");//WebP
            imageSuffixes.add("FLIC");
            imageSuffixes.add("EMF");
            imageSuffixes.add("ICO");
            imageSuffixes.add("HEIF");
            imageSuffixes.add("AI");


            //https://en.wikipedia.org/wiki/Image_file_formats#Compound_formats_(see_also_Metafile)
            imageSuffixes.add("TIFF");
            imageSuffixes.add("DNG");

            //***************************************************

            //无损格式，例如WAV，FLAC，APE，ALAC，WavPack(WV)
            audioSuffixes.add("WAV");
            audioSuffixes.add("FLAC");
            audioSuffixes.add("APE");
            audioSuffixes.add("ALAC");
            audioSuffixes.add("WV");

            //有损格式，例如MP3，AAC，Ogg Vorbis，Opus
            audioSuffixes.add("MP3");
            audioSuffixes.add("AAC");
            audioSuffixes.add("OGG");

            //
            audioSuffixes.add("WMA");
            audioSuffixes.add("RM");
            audioSuffixes.add("M4A");
            audioSuffixes.add("DTS");
            audioSuffixes.add("DSF");
            audioSuffixes.add("DFF");


            //***************************************************
            videoSuffixes.add("MP4");
            videoSuffixes.add("MKV");
            videoSuffixes.add("MOV");
            videoSuffixes.add("TS");
            videoSuffixes.add("AVI");

            //***************************************************

            textSuffixes.add("KTS");
            textSuffixes.add("LOG");
            textSuffixes.add("JAVA");
            textSuffixes.add("IN");
            textSuffixes.add("TXT");
            textSuffixes.add("MD");
            textSuffixes.add("HTML");
            textSuffixes.add("JSON");
            textSuffixes.add("YML");
            textSuffixes.add("YAML");
            textSuffixes.add("PROPERTIES");
            textSuffixes.add("CONF");
            textSuffixes.add("SH");
            textSuffixes.add("js");


            //***************************************************
            officeSuffixes.add("XLS");
            officeSuffixes.add("XLSX");
            officeSuffixes.add("DOC");
            officeSuffixes.add("DOCX");
            officeSuffixes.add("PPT");
            officeSuffixes.add("PPTX");

            //***************************************************
            zipSuffixes.add("RAR");
            zipSuffixes.add("ZIP");
            zipSuffixes.add("7ZIP");
            zipSuffixes.add("TAR");


        }

    }

    public static class FileName {
        String sourceFileName;
        String prefix;
        String suffix;

        public FileName(String sourceFileName, String prefix, String suffix) {
            this.sourceFileName = sourceFileName;
            this.prefix = prefix;
            this.suffix = suffix;
        }

        public String getSourceFileName() {
            return sourceFileName;
        }

        public void setSourceFileName(String sourceFileName) {
            this.sourceFileName = sourceFileName;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getSuffix() {
            return suffix;
        }

        public void setSuffix(String suffix) {
            this.suffix = suffix;
        }

        @Override
        public String toString() {

            return "{" + "\"sourceFileName\":" + sourceFileName + "," + "\"prefix\":" + prefix + "," + "\"suffix\":" + suffix + "}\n";
        }
    }
}
