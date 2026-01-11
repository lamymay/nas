package com.arc.nas.service.system.common;

import com.arc.nas.model.domain.system.common.SysFile;
import com.arc.nas.model.request.app.media.SysFilePageable;
import com.arc.nas.model.request.app.media.SysFileQuery;
import org.springframework.data.domain.Page;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 文件操作DAO
 *
 * @author may
 * @since 2021.12.09 11:40 下午
 */
public interface SysFileDAO {

    SysFile saveOne(SysFile record);

    boolean saveAll(List<SysFile> records);

    boolean update(SysFile record);

    boolean updateAll(List<SysFile> records);

    boolean updateAllByCodes(List<SysFile> records);

    int deleteById(Long id);

    int deleteByIds(List<Long> ids);

    SysFile getById(Long id);

    SysFile getByCode(String code);

    SysFile getByIdOrCode(String idOrCode);

    List<SysFile> list(SysFile query);

    Page<SysFile> listPage(SysFilePageable pageable);

    List<SysFile> listAllByQuery(SysFileQuery query);

    long count(SysFilePageable request);

    List<SysFile> listAll();

    List<SysFile> listAllByStatus(int status);

    SysFile getByName(String name);

    List<SysFile> listLikeDisplayName(String fileName);

    long countByStatus(Integer status);

    List<SysFile> getByHash(String hash);

    SysFile getByHashAndName(String sha256Hash, String name);

    List<SysFile> listAllByMediaType(String mediaType);

    List<SysFile> listAllByMediaTypes(Collection<String> mediaTypes);

    List<SysFile> listAllByCodes(Set<String> codes);

    Boolean deleteByCodes(Set<String> codeSet);

    List<SysFile> listFilesByMediaTypesAndTags(SysFileQuery query);

    List<SysFile> listByCode(String code);
}

//
//    /**
//     * 文件持久化并在数据库做记录
//     * 注意文件名称保证不相同，不存在重复文件覆盖问题，同时带来一个问题，前端相同文件重复上传造成服务端资源浪费，建议用定时线程去清理无效的重复文件
//     * 记录日志
//     * 判合法性，非空，大小，格式
//     * 1、文件写入磁盘,注意文件不会被覆盖，因为不存在同名文件
//     * 2、描述信息记录数据库
//     *
//     * @param file    文件
//     * @param tempDir 目录
//     * @return 数据库标记的code，用于查询
//     */
//    public SysFile writeFileToDiskAndRecord(MultipartFile file, String tempDir, String profile, String protocol, String contextPath, String port) {
//
//        //需求判断文件是否为空
//        if (file.isEmpty()) {
//            return null;
//        }
//        log.debug("文件上传入参: 类型={}，名称={}，尺寸={} bytes", file.getContentType(), file.getOriginalFilename(), file.getSize());
//
//        //文件落地--文件名称 文路径
//        //存在该文件夹吗？
//        //是文件夹吗？
//        File outFile = new File(tempDir);
//
//        if (!outFile.exists()) {
//            // boolean mkdir() :  创建此抽象路径名指定的目录。 父级路径若不存在则不会创建该目录。
//            // boolean mkdirs() :  创建此抽象路径名指定的目录，包括创建必需但不存在的父目录。父级路径若不存在则会创建该目录。
//            boolean mkdirs = outFile.mkdirs();
//            if (!mkdirs) {
//                throw new RuntimeException("文件夹不存在，并创建失败，文件终止保存");
//            }
//        }
//
//        //getAbsolutePath()  方法去除了干扰   ./   ../
//        String writeFile = outFile.getAbsolutePath() + File.separator + FileUtil.builtUniqueFilenameOverTimestamp(file.getOriginalFilename());
//        String toDiskPath = null;
//        try {
//            toDiskPath = FileUtil.writeToDisk(file.getInputStream(), writeFile);
//        } catch (IOException e) {
//            log.error("error={}", tempDir, e);
//        }
//        if (toDiskPath != null) {
//            // 构建可入库的数据
//
//            String host;
//            log.debug("结果={}", profile);
//            log.info("profile={}", profile);
//            log.info("profile={}", profile);
//            log.info("profile={}", profile);
//
//            if ("self".equals(profile)) {
//                host = "122.51.110.127";
//                //因为数据库账号密码的问题  self [限制为%]与 160[限制指定IP] 的是不一样的 当本地调试的时候需要切换 注释
//            } else if ("160".equals(profile)) {
//                host = "122.51.110.127";
//            } else {
//                host = "127.0.0.1";
//            }
//
//            String os = System.getenv("OS");
//            log.debug("os 结果={}", os);
//            log.debug("os 结果={}", os);
//            log.info("os 结果={}", os);
//            log.info("os 结果={}", os);
//
//            if ("Windows_NT".equals(os)) {
//                host = "127.0.0.1";
//            }
//
//
////                try {
////                    host = InetAddress.getLocalHost().getHostAddress();//获得本机IP
////                    log.debug("Java获取本机ip和服务器ip={}",host);
////                    log.debug("Java获取本机ip和服务器ip={}",host);
////                    log.info("Java获取本机ip和服务器ip={}",host);
////                    log.info("Java获取本机ip和服务器ip={}",host);
////                    log.info("Java获取本机ip和服务器ip={}",host);
////                } catch (UnknownHostException e) {
//////                    e.printStackTrace();
////                    throw new BizException(e);
////                }
//            //   sysFile.getCode()  =  文件名称+后缀   绝对路径  就是 path     资源名称 就是 uri+ 文件全名称
//
//            SysFile sysFile = new SysFile(file, toDiskPath);
//
//            return sysFile;
//        }
//
//    }




/*

    public String getFormatLength() {
        if (null == this.getLengthUnit() || "".equals(this.getLengthUnit())) {
            return LengthUtil.formatFileLength(length);
        } else {

            return this.getLengthUnit();
        }
    }
*/


//URI = Universal Resource Identifier 统一资源标志符，用来标识抽象或物理资源的一个紧凑字符串。
//URL = Universal Resource Locator 统一资源定位符，一种定位资源的主要访问机制的字符串，一个标准的URL必须包括：protocol、host、port、path、parameter、anchor。
//URN = Universal Resource Name 统一资源名称，通过特定命名空间中的唯一名称或ID来标识资源。
//————————————————
//版权声明：本文为CSDN博主「koflance@西溪」的原创文章，遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接及本声明。
//原文链接：https://blog.csdn.net/koflance/article/details/79635240

