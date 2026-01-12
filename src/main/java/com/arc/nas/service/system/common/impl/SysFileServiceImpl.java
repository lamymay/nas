package com.arc.nas.service.system.common.impl;

import com.arc.nas.model.domain.system.common.SysFile;
import com.arc.nas.model.request.app.media.BatchItemResult;
import com.arc.nas.model.request.app.media.BatchResult;
import com.arc.nas.model.request.app.media.SysFilePageable;
import com.arc.nas.model.request.app.media.SysFileQuery;
import com.arc.nas.service.system.common.SysFileDAO;
import com.arc.nas.service.system.common.SysFileService;
import com.arc.util.Assert;
import com.arc.util.JSON;
import com.arc.util.file.FileSameCheckTool;
import com.arc.util.file.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.arc.nas.init.ReadyResourceInit.getWriteableDirectory;
import static com.arc.util.file.FileUtil.detectionTargetFileV3;

@Service
public class SysFileServiceImpl implements SysFileService {

    private static final Logger log = LoggerFactory.getLogger(SysFileServiceImpl.class);
    static long maxLen = 200 * 1024 * 1024;

    private final SysFileDAO sysFileDAO;

    public SysFileServiceImpl(SysFileDAO sysFileDAO) {
        this.sysFileDAO = sysFileDAO;
    }

    /**
     * 判断字符串是不字
     *
     * @param str 字符串
     * @return true=是数字/false=非数字
     */
    public static boolean isNumberString(String str) {
        if (str == null || str.trim().length() == 0) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    /**
     * BIO方式拷贝
     *
     * @param in  输入
     * @param out 输出
     * @return int int
     * @throws IOException IOException
     */
    public static int copy(InputStream in, OutputStream out) throws IOException {
        if (in == null) throw new RuntimeException("No InputStream specified");
        if (out == null) throw new RuntimeException("No OutputStream specified");

        int byteCount = 0;
        byte[] buffer = new byte[4096];

        int bytesRead;
        for (boolean var4 = true; (bytesRead = in.read(buffer)) != -1; byteCount += bytesRead) {
            out.write(buffer, 0, bytesRead);
        }
        out.flush();
        return byteCount;
    }

//    @Override
//    public Long save(SysFile sysFile) {
//        String sha256 = FileSameCheckTool.calculateHashSHA256(new File(sysFile.getPath()));
//        SysFile exist = getByIdOrCode(sha256);
//        if (exist != null) {
//            //  存在相同的文件了
//            return exist.getId();
//        }
//
//        if (sysFile.getCode() == null || sysFile.getCode().trim().isEmpty()) {
//            log.warn(" 异常   sysFile.getCode() == nul");
//            sysFile.setCode(sha256);
//        }
//        if (sysFile.getHash() == null) {
//            sysFile.setHash(sha256);
//        }
//
//        SysFile saveOne = sysFileDAO.saveOne(sysFile);
//        return saveOne == null ? null : saveOne.getId();
//    }

    private void setCommon(SysFile sysFile) {
        if (sysFile != null) {
            sysFile.setHash(FileSameCheckTool.calculateHashSHA256(new File(sysFile.getPath())));
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteById(Long id) {
        SysFile existFileIndex = sysFileDAO.getById(id);
        if (existFileIndex == null) {
            throw new RuntimeException("删除的文件不存在");
        } else {
            boolean deleted = FileUtil.deleteFile(new File(existFileIndex.getPath()), true);
            if (deleted) {
                return sysFileDAO.deleteById(id);
            } else {
                return false;
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByCode(String code) {
        SysFile existFileIndex = sysFileDAO.getByCode(code);
        if (existFileIndex == null) {
            throw new RuntimeException("删除的文件不存在");
        } else {
            try {

                boolean deleted = FileUtil.deleteFile(new File(existFileIndex.getPath()), true);
                if (deleted) {
                    return sysFileDAO.deleteById(existFileIndex.getId());
                } else {
                    return false;
                }
            } catch (Exception exception) {
                log.error("", exception);
                boolean deleted = FileUtil.deleteFile(new File(existFileIndex.getPath()), false);
                if (deleted) {
                    return sysFileDAO.deleteById(existFileIndex.getId());
                } else {
                    return false;
                }
            }
        }
    }

    @Override
    public Map<String, BatchItemResult> deleteByCodes(String... codes) {

        // 最终返回的结果
        Map<String, BatchItemResult> resultMap = new HashMap<>();

        if (codes == null || codes.length == 0) {
            return resultMap;
        }

        try {
            Set<String> codeSet = new HashSet<>(List.of(codes));
            List<SysFile> sysFiles = sysFileDAO.listAllByCodes(codeSet);

            // DB 能找到的文件 map，方便查找
            Map<String, SysFile> sysFileMap = sysFiles.stream()
                    .collect(Collectors.toMap(SysFile::getCode, f -> f));

            Set<String> needDeleteDbCodes = new HashSet<>();

            // 1. 先处理每个 code 的文件删除
            for (String code : codeSet) {

                SysFile sysFile = sysFileMap.get(code);
                if (sysFile == null) {
                    // 数据库没有记录
                    resultMap.put(code, new BatchItemResult(false, "Record not found in DB."));
                    continue;
                }

                File file = new File(sysFile.getPath());
                boolean deleted = FileUtil.deleteFile(file, false);

                if (!deleted) {
                    resultMap.put(code, new BatchItemResult(false, "Failed to delete local file."));
                    continue;
                }

                // 文件删除成功，下一步要删数据库
                needDeleteDbCodes.add(code);
            }

            // 2. 删除数据库中成功删除文件的记录
            if (!needDeleteDbCodes.isEmpty()) {
                boolean dbDeleteOk = sysFileDAO.deleteByCodes(needDeleteDbCodes);

                if (!dbDeleteOk) {
                    // DB 删除失败 → 逐条标记
                    for (String code : needDeleteDbCodes) {
                        resultMap.put(code, new BatchItemResult(false, "File deleted but failed to delete DB record."));
                    }
                } else {
                    // DB 删除成功 → 标记成功
                    for (String code : needDeleteDbCodes) {
                        resultMap.put(code, new BatchItemResult(true));
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error deleting files: ", e);
            // 出异常：所有 files 标记失败
            Map<String, BatchItemResult> errMap = new HashMap<>();
            for (String code : codes) {
                errMap.put(code, new BatchItemResult(false, "Exception: " + e.getMessage()));
            }
            return errMap;
        }

        return resultMap;
    }

    @Override
    public boolean update(SysFile sysFile) {
        setCommon(sysFile);
        sysFile.setUpdateTime(new Date());

        return sysFileDAO.update(sysFile);
    }

    @Override
    public boolean updateAll(List<SysFile> records) {
        return sysFileDAO.updateAll(records);
    }

    @Override
    public BatchResult updateAllByCodes(List<SysFile> records) {
        HashMap<String, BatchResult> resultMap = new HashMap<>();
        if (records == null || records.isEmpty()) {
            return new BatchResult(false, "参数错误：空参数");
        }
        // todo 按照传入字段有值的字段才更新，否则不动数据库中的字段
        boolean updateAll = sysFileDAO.updateAllByCodes(records);
        return new BatchResult(updateAll, "参数错误：空参数");
    }

    @Override
    public SysFile get(Long id) {
        return sysFileDAO.getById(id);
    }

    @Override
    public List<SysFile> listByCode(String code) {
        return sysFileDAO.listByCode(code);
    }

    @Override
    public List<SysFile> listByHash(String hash) {
        return sysFileDAO.listByCode(hash);
    }

    /**
     * 文件持久化并在数据库做记录
     * 注意文件名称保证不相同，不存在重复文件覆盖问题，同时带来一个问题，前端相同文件重复上传造成服务端资源浪费，建议用定时线程去清理无效的重复文件
     * 记录日志
     * 判合法性，非空，大小，格式
     * 1、文件写入磁盘,注意文件不会被覆盖，因为不存在同名文件
     * 2、描述信息记录数据库
     *
     * @param multipartFile 文件
     * @return 数据库标记的code，用于查询
     */
    @Override
    public SysFile writeFileToLocalDiskAndCreateDBIndex(final MultipartFile multipartFile) {

        //需求判断文件是否为空
        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }

        debugShow(multipartFile);
        boolean useCache = multipartFile.getSize() <= maxLen;
        if (useCache) {
            return createFileIndex(multipartFile);

        } else {
            return createLargeFileIndex(multipartFile);

        }
    }

    private SysFile createLargeFileIndex(MultipartFile multipartFile) {
        try {
            final File diskFile = detectionTargetFileV3(getWriteableDirectory(), multipartFile.getOriginalFilename(), "");
            log.info("diskFile to \n{}", diskFile);
            FileUtil.writeToDiskWithTry(multipartFile.getInputStream(), diskFile);
            String sha256Hash = FileSameCheckTool.calculateHashSHA256(diskFile);

            SysFile existFileIndex = sysFileDAO.getByHashAndName(sha256Hash, multipartFile.getOriginalFilename());

            if (existFileIndex != null) {
                // 1.1 hash相同 文件名称相同 一模一样的文件，不必创建新的文件索引，只用更新下引用计数器即可
                log.info("重复文件,文件名称都一某一样,{}", JSON.toJSONString(existFileIndex));
                int tempCount = existFileIndex.getReferenceCount() == null ? 0 : existFileIndex.getReferenceCount();
                existFileIndex.setReferenceCount(tempCount + 1);
                sysFileDAO.update(existFileIndex);

                // 删除大文件
                diskFile.deleteOnExit();
                return existFileIndex;
            } else {
                // hash「不相同」 且是「大」文件， 之前已经写盘了，此时不必再次写盘

                // 并创建文件索引信息
                SysFile sysFile = createSysFile(multipartFile, diskFile);
                sysFile.setVersion(1);
                sysFileDAO.saveOne(sysFile);
                return sysFile;
            }
        } catch (IOException exception) {
            log.error(" FileUtil.writeToDisk(file.getInputStream(), writeFile); ", exception);
            return null;
        } finally {
            System.gc();
        }
    }

    private SysFile createFileIndex(MultipartFile multipartFile) {
        byte[] cache;
        try {
            //  避免重复写磁盘 对于小文件采取内存缓存的方式优化 从MultipartFile中读取数据并缓存在内存中
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            InputStream inputStream = multipartFile.getInputStream();
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            cache = outputStream.toByteArray();
            String sha256Hash = FileSameCheckTool.calculateHashSHA256(new ByteArrayInputStream(cache));
            SysFile existFileIndex = sysFileDAO.getByHashAndName(sha256Hash, multipartFile.getOriginalFilename());

            // 1.1 hash相同 文件名称相同 一模一样的文件，不必创建新的文件索引，只用更新下引用计数器即可
            if (existFileIndex != null) {
                // 1.1 hash相同 文件名称相同 一模一样的文件，不必创建新的文件索引，只用更新下引用计数器即可
                log.info("重复文件,文件名称都一某一样,{}", JSON.toJSONString(existFileIndex));
                int tempCount = existFileIndex.getReferenceCount() == null ? 0 : existFileIndex.getReferenceCount();
                existFileIndex.setReferenceCount(tempCount + 1);
                sysFileDAO.update(existFileIndex);
                return existFileIndex;

            } else {

                // 2.1 hash「不相同」 且是小文件， 现在写入磁盘
                File writeableDirectory = getWriteableDirectory();
                String originalFilename = multipartFile.getOriginalFilename();
                final File diskFile = detectionTargetFileV3(writeableDirectory, originalFilename, "");
                log.info("diskFile to \n{}", diskFile);
                FileUtil.writeToDiskWithTry(new ByteArrayInputStream(cache), diskFile);
                // else的逻辑  2.2 hash「不相同」 且是「大」文件， 之前已经写盘了，此时不必再次写盘

                // 并创建文件索引信息
                SysFile sysFile = createSysFile(multipartFile, diskFile);
                sysFile.setVersion(1);
                sysFileDAO.saveOne(sysFile);
                return sysFile;

            }

        } catch (IOException exception) {
            log.error(" FileUtil.writeToDisk(file.getInputStream(), writeFile); ", exception);
            return null;
        } finally {
            cache = null;
            System.gc();
        }

    }

    private void debugShow(MultipartFile multipartFile) {
        log.debug("getName={}", multipartFile.getName());//变量的名称 multipartFile
        log.debug("getResource={}", multipartFile.getResource());//MultipartFile resource [file]
        log.debug("getContentType={}", multipartFile.getContentType());
        log.debug("getOriginalFilename={}", multipartFile.getOriginalFilename());//记录.docx
        log.info("文件上传入参: 类型={}，名称={}，size={}bytes", multipartFile.getContentType(), multipartFile.getOriginalFilename(), multipartFile.getSize());

    }

    /**
     * 构建可入库的数据
     *
     * @param file       MultipartFile
     * @param toDiskPath toDiskPath
     * @return SysFile
     */
    private SysFile createSysFile(MultipartFile file, File toDiskPath) {
        //全名
        String name = file.getOriginalFilename();
        String displayName = FileUtil.getFilenameWithoutExtension(name);
        String extension = FileUtil.getExtension(name);
        String type = FileUtil.getFileType(extension);
        String sha256 = FileSameCheckTool.calculateHashSHA256(new File(toDiskPath.getPath()));
        SysFile sysFile = new SysFile(sha256, displayName, extension, type, "", name, file.getSize(), toDiskPath.getPath());
        sysFile.setRemark("文件上传");

        return sysFile;
    }

    @Override
    public SysFile getById(Long id) {
        return sysFileDAO.getById(id);
    }

    private void concurrencyCleanAsync(SysFile sysFile) {
        Assert.notNull(sysFile, "将要删除的文件不为空！");
        Assert.notNull(sysFile.getPath(), "将要删除的文件路径不为空！");
        boolean delete = FileUtil.deleteFile(sysFile.getPath());
        log.info("文件SysFile={},路径={}删除={}", JSON.toJSONString(sysFile), sysFile.getPath(), (delete ? "成功" : "失败"));
    }

//    @Override
//    public boolean saveAll(List<SysFile> files) {
//        files.stream()
//                .peek(sysFile -> {
//                    if (sysFile.getCode() == null || sysFile.getCode().trim().isEmpty()) {
//                        sysFile.setCode(FileSameCheckTool.calculateHashSHA256(new File(sysFile.getPath())));
//                    }
//                    if (sysFile.getHash() == null) {
//                        setCommon(sysFile);
//                    }
//                });
////                .collect(Collectors.toList());// collect 可以不用，peek 本身已经修改了原对象
//        return sysFileDAO.saveAll(files);
//
//    }

    @Override
    public SysFile getByIdOrCode(Object idOrCode) {
        log.debug("文件下载，参数接受 code / id={}", idOrCode);
        String idString = String.valueOf(idOrCode);
        SysFile sysFile;
        if (isNumberString(idString)) {
            sysFile = sysFileDAO.getById(Long.parseLong(idString));

        } else {
            sysFile = sysFileDAO.getByCode(idOrCode.toString());
        }

        return sysFile;
    }

    @Override
    public File download(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("multipart/form-data;charset=UTF-8"));
            RequestCallback requestCallback = request -> request.getHeaders().setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));

            // 对响应进行流式处理而不是将其全部加载到内存中
            String basePath = System.getProperty("java.io.tmpdir");
            File baseFile = new File(basePath);
            if (!baseFile.exists()) {
                baseFile.mkdirs();
            }

            log.info("url={}", url);
            return new RestTemplate().execute(url, HttpMethod.GET, requestCallback, clientHttpResponse -> {
                HttpHeaders responseHeaders = clientHttpResponse.getHeaders();
                //"Content-Disposition":["attachment;fileName=IMAGE11648311194583.png"]
                List<String> messages = responseHeaders.get("Content-Disposition");
                String filename;
                if (messages != null && messages.get(0) != null) {
                    filename = messages.get(0);
                    filename = basePath + File.separator + filename.substring(filename.indexOf("=") + 1);
                } else {
                    filename = basePath + File.separator + UUID.randomUUID().toString().replace("-", "") + ".xlsx";
                }
                log.info("responseHeaders={}", JSON.toJSONString(responseHeaders));
                log.info("rawStatusCode={},clientHttpResponse.getStatusCode()={},clientHttpResponse.getStatusText()={}"
                        , clientHttpResponse.getRawStatusCode()
                        , clientHttpResponse.getStatusCode()
                        , clientHttpResponse.getStatusText()
                );
                FileOutputStream fileOutputStream = new FileOutputStream(filename);
                copy(clientHttpResponse.getBody(), fileOutputStream);
                fileOutputStream.close();
                return new File(filename);
            });

        } catch (Exception exception) {
            log.error("error when ", exception);
            throw new RuntimeException(exception);
        }

    }

    @Override
    public Page<SysFile> listPage(SysFilePageable pageable) {
        return sysFileDAO.listPage(pageable);
    }

    @Override
    public SysFile getByName(String name) {
        return sysFileDAO.getByName(name);
    }

    @Override
    public List<SysFile> listAll() {
        return sysFileDAO.listAll();

    }

    @Override
    public List<SysFile> listLikeDisplayName(String fileName) {
        return sysFileDAO.listLikeDisplayName(fileName);

    }

    @Override
    public List<SysFile> listAllByMediaType(String mediaType) {
        return sysFileDAO.listAllByMediaType(mediaType);

    }

    @Override
    public Map<String, Map<String, SysFile>> listAllByMediaTypes(String... mediaTypes) {
        Set<String> mediaTypeSet = Arrays.stream(mediaTypes).collect(Collectors.toSet());
        List<SysFile> fileList = sysFileDAO.listAllByMediaTypes(mediaTypeSet);
        return convertToMap(mediaTypeSet, fileList);
    }

    private Map<String, Map<String, SysFile>> convertToMap(Set<String> mediaTypes, List<SysFile> fileList) {
        Set<String> targetTypes = mediaTypes.stream().filter(Objects::nonNull).map(String::toUpperCase).collect(Collectors.toSet());

        if (fileList == null || fileList.isEmpty()) {
            return Collections.emptyMap();
        }

        return fileList.stream()
                .filter(f -> f.getMediaType() != null
                        && targetTypes.contains(f.getMediaType().toUpperCase()))
                .collect(Collectors.groupingBy(
                        f -> f.getMediaType().toUpperCase(),
                        Collectors.toMap(
                                SysFile::getCode,
                                f -> f,
                                // 冲突处理逻辑：(existingValue, newValue) -> 返回值
                                (existingValue, newValue) -> existingValue // 保留第一个发现的 code，忽略后续重复的
                                // 或者用 (existing, replacement) -> replacement // 覆盖为最新的 code
                        )
                ));
    }

    @Override
    public Map<String, Map<String, SysFile>> listAllByQuery(SysFileQuery query) {
        List<SysFile> fileList = sysFileDAO.listFilesByMediaTypesAndTags(query);
        return convertToMap(query.getMediaTypes(), fileList);
    }


}

