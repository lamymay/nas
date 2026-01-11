package com.arc.nas.service.app.media.impl;

import cn.hutool.core.bean.BeanUtil;
import com.arc.nas.model.domain.app.media.FileTagRelation;
import com.arc.nas.model.domain.system.common.SysFile;
import com.arc.nas.model.dto.app.media.MediaItemDTO;
import com.arc.nas.model.dto.app.media.MediaPageDTO;
import com.arc.nas.model.request.app.media.*;
import com.arc.nas.repository.mysql.dao.app.FileTagRelationDAO;
import com.arc.nas.service.app.media.MediaResource;
import com.arc.nas.service.app.media.MediaService;
import com.arc.nas.service.system.common.SysFileDAO;
import com.arc.nas.service.system.common.SysFileService;
import com.arc.util.Assert;
import com.arc.util.JSON;
import com.arc.util.StringTool;
import com.arc.util.file.FileSameCheckTool;
import com.arc.util.file.FileUtil;
import com.arc.util.file.Platform;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.arc.nas.model.domain.system.common.SysFile.createSysFile;
import static com.arc.nas.model.domain.system.common.SysFile.createSysFileSimple;
import static com.arc.nas.service.system.common.SysFileService.*;
import static com.arc.util.file.FFmpegThumbnailGenerator.generateImageThumbnail;
import static com.arc.util.file.FFmpegThumbnailGenerator.generateVideoThumbnails;

@Service
public class MediaServiceImpl implements MediaService {

    public final static String nomedia = ".nomedia";
    public final static String ignore = ".ignore";
    final static String thumbnailsDefaultFolderName = ".thumbnails.bundle";
    final static String userHome = System.getProperty("user.home");
    private static final Logger log = LoggerFactory.getLogger(MediaServiceImpl.class);
    private final SysFileService fileService;
    private final SysFileDAO sysFileDAO;
    private final FileTagRelationDAO fileTagRelationDAO;
    private final UrlHelper urlHelper;
    private final MediaResource mediaResource;
    private final ListeningExecutorService executor;
    ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(2);


    public MediaServiceImpl(SysFileService fileService, SysFileDAO sysFileDAO, FileTagRelationDAO fileTagRelationDAO,
                            UrlHelper urlHelper,
                            MediaResource mediaResource) {
        this.fileService = fileService;
        this.sysFileDAO = sysFileDAO;
        this.fileTagRelationDAO = fileTagRelationDAO;
        this.urlHelper = urlHelper;
        this.mediaResource = mediaResource;
        // 创建 Guava ListeningExecutorService，线程池大小 = CPU 核数
        this.executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
    }

    @Override
    public Integer scan(String... folders) {
        log.info("开始扫描同步, 参数={}", JSON.toJSONString(folders));
        if (folders == null ) return 0;

        // 1. 扫描磁盘：获取当前所有文件的最新状态
        List<File> allDiskFiles = new ArrayList<>();
        HashSet<String> ignoreFile = new HashSet<>();
        ignoreFile.add(nomedia);
        ignoreFile.add(ignore);
        ignoreFile.addAll(Platform.SYSTEM_DEFAULT_FILENAMES);
        for (String folder : folders) {
            List<File> tmp = FileUtil.listFileByDir(folder, ignoreFile);
            if (tmp != null) allDiskFiles.addAll(tmp);
        }

        String thumbnailRootFolder = getThumbnailRoot().getAbsolutePath();
        List<File> tmpThumbnail = FileUtil.listFileByDir(thumbnailRootFolder, ignoreFile);
        if (tmpThumbnail != null) allDiskFiles.addAll(tmpThumbnail);


        // 将磁盘文件转为 Map，Key 为绝对路径，方便快速比对
        Map<String, File> diskPathMap = allDiskFiles.stream()
                .collect(Collectors.toMap(File::getAbsolutePath, f -> f, (o, n) -> n));

        // 2. 获取数据库索引
        List<SysFile> index = sysFileDAO.listAll();
        Map<String, SysFile> indexPathMap = index.stream()
                .collect(Collectors.toMap(SysFile::getPath, Function.identity(), (o, n) -> n));

        List<SysFile> toDelete = new ArrayList<>();
        List<SysFile> toInsert = new ArrayList<>();
        List<SysFile> toUpdate = new ArrayList<>(); // 用于处理文件内容变化（大小或时间变了）

        // 3. 计算【删除】：找出数据库里有，但磁盘上已经消失的
        for (SysFile dbFile : index) {
            if (dbFile == null) continue;
            if (!diskPathMap.containsKey(dbFile.getPath())) {
                toDelete.add(dbFile);
            }
        }

        // 4. 计算【新增】与【更新】：找出磁盘上有，但数据库没有或已过期的
        for (File file : allDiskFiles) {
            String path = file.getAbsolutePath();
            SysFile dbFile = indexPathMap.get(path);

            if (dbFile == null) {
                // 情况A：纯新增
                toInsert.add(createSysFileSimple(file, "PENDING"));
            } else {
                // 情况B：路径虽然存在，但检查文件是否被替换/修改过 (比对大小和最后修改时间)
                if (file.length() != dbFile.getLength() ||
                        file.lastModified() != dbFile.getUpdateTime().getTime()) {

                    // 更新元数据，并将 Hash 标记为待重新计算
                    dbFile.setLength(file.length());
                    dbFile.setUpdateTime(new Date(file.lastModified()));
                    dbFile.setHash(null); // 清空旧 Hash
                    dbFile.setTaskStatus("PENDING");
                    toUpdate.add(dbFile);
                }
            }
        }


        // 5. 执行数据库操作
        if (!toDelete.isEmpty()) {
            log.info("扫描-toDelete-清理失效索引: {} 条", toDelete.size());
            deleteAll(toDelete); // 记得在此方法内同步删除 H2 里的元数据
        }

        if (!toInsert.isEmpty()) {
            log.info("扫描-toInsert-发现新文件: {} 条", toInsert.size());
            sysFileDAO.saveAll(toInsert);
        }

        if (!toUpdate.isEmpty()) {
            log.info("扫描-toUpdate-检测到内容变更: {} 条", toUpdate.size());
            sysFileDAO.updateAll(toUpdate);
        }

        return toInsert.size() + toUpdate.size();
    }

    @Override
    public void updateHash(boolean force) {
        log.info("开始updateHash");
        List<SysFile> index = sysFileDAO.listAll();
        if (index == null || index.isEmpty()) return;

        for (SysFile sysFile : index) {
            if (sysFile == null) continue;
            String path = null;
            if (StringTool.isBlank(sysFile.getHash())) {
                path = sysFile.getPath();
            }

            if (force) {
                path = sysFile.getPath();
            }

            if (path != null) {
                String hashSHA256 = FileSameCheckTool.calculateHashSHA256(new File(path));
                sysFile.setCode(hashSHA256);
                sysFile.setHash(hashSHA256);
                sysFile.setTaskStatus("HASH_DONE");
                sysFileDAO.update(sysFile);

            }
        }

    }

    private void deleteAll(List<SysFile> toDelete) {
        int cunt = 0;
        if (!toDelete.isEmpty()) {
            for (SysFile sysFile : toDelete) {
                log.info("### 删除索引 delete sysFile={}", JSON.toJSONString(sysFile));
                // 1 处理缩略图
                String thumbnail = sysFile.getThumbnail();
                if (thumbnail != null) {
                    for (String thumbnailFileCode : thumbnail.split(",")) {
                        thumbnailFileCode = thumbnailFileCode.trim();
                        SysFile thumbnailFile = fileService.getByIdOrCode(thumbnailFileCode);
                        if (thumbnailFile != null && thumbnailFile.getPath() != null) {
                            FileUtil.deleteFile(thumbnailFile.getPath());
                        }
                        fileService.deleteByCode(thumbnailFileCode);
                    }
                }
                boolean deleted = false;
                if ("THUMBNAIL".equals(sysFile.getMediaType())) {
                    // 2.1  缩略图的情况下 删除文件索引 & 删除文件
                    deleted = fileService.deleteById(sysFile.getId());
                } else {
                    // 2.2  非缩略图的情况下 只删除文件索引 不删除文件系统的原始文件（但是文件的缩略图在第一步已经被删除了）
                    deleted = sysFileDAO.deleteById(sysFile.getId());
                }

                if (deleted) {
                    cunt = cunt + 1;
                }
            }
            log.info("删除文件索引 cunt={} toDelete={}", cunt, toDelete.size());
        } else {
            log.info("删除文件索引 null");
        }
    }


    // 延迟执行 计算hash 缩略图
    // scheduledExecutor.schedule(() -> matchAndGenerateThumbnailsInner(), 5, TimeUnit.SECONDS);


    /**
     * 自动匹配缩略图
     */
    @Override
    public void autoMatchThumbnails() {

        List<SysFile> videos = sysFileDAO.listAllByMediaType(VIDEO);
        List<SysFile> images = sysFileDAO.listAllByMediaType(IMAGE);
        List<SysFile> thumbnails = sysFileDAO.listAllByMediaType(THUMBNAIL);

        // key=originalName
        Map<String, SysFile> videoMap = videos == null ? Collections.emptyMap() : videos.stream().collect(Collectors.toMap(f -> FileUtil.getFilenameWithoutExtension(f.getOriginalName()),  // key
                f -> f,                                                    // value
                (a, b) -> a                                                // 解决 key 冲突时返回前者
        ));
        Map<String, SysFile> imageMap = images == null ? Collections.emptyMap() : images.stream().collect(Collectors.toMap(f -> FileUtil.getFilenameWithoutExtension(f.getOriginalName()),  // key
                f -> f,                                                    // value
                (a, b) -> a                                                // 解决 key 冲突时返回前者
        ));
        Map<String, SysFile> thumbnailMap = thumbnails == null ? Collections.emptyMap() : thumbnails.stream().collect(Collectors.toMap(f -> FileUtil.getFilenameWithoutExtension(f.getOriginalName()),  // key
                f -> f,                                                    // value
                (a, b) -> a                                                // 解决 key 冲突时返回前者
        ));


        if (imageMap != null && !thumbnailMap.isEmpty()) {
            imageMap.putAll(thumbnailMap);
        }
        List<SysFile> toUpdate = new ArrayList<>();

        // 匹配缩略图
        for (Map.Entry<String, SysFile> entry : videoMap.entrySet()) {
            String baseName = entry.getKey();
            SysFile videoFile = entry.getValue();

            if (imageMap.containsKey(baseName)) {
                SysFile thumb = imageMap.get(baseName);

                // 更新视频行
                String newThumbnail = thumb.getCode();
                if (videoFile.getThumbnail() != null) {
                    Set<String> thumbnailsSet = new LinkedHashSet<>(Arrays.asList(videoFile.getThumbnail().split(",")));
                    thumbnailsSet.add(newThumbnail);
                    videoFile.setThumbnail(String.join(",", thumbnailsSet));
                } else {
                    videoFile.setThumbnail(newThumbnail);
                }
                toUpdate.add(videoFile);
                // 更新缩略图行
                thumb.setMediaType("THUMBNAIL");
                toUpdate.add(thumb);

                log.info("视频 {} 匹配缩略图 {} 成功", videoFile.getOriginalName(), thumb.getOriginalName());
            }
        }

        if (!toUpdate.isEmpty()) {
            fileService.updateAll(toUpdate);
            log.info("批量更新 {} 条视频和缩略图记录", toUpdate.size());
        }
    }

    @Override
    public MediaPageDTO listPage(SysFilePageable pageable) {
        Page<SysFile> page = fileService.listPage(pageable);
        MediaPageDTO mediaPageDTO = new MediaPageDTO();
        mediaPageDTO.setContent(urlHelper.covertSysFileToMediaItemDTO(page.getContent()));
        mediaPageDTO.setPageNumber(pageable.getPageNumber());
        mediaPageDTO.setPageSize(pageable.getPageSize());
        mediaPageDTO.setTotalElements(page.getTotalElements());
        mediaPageDTO.setTotalPages(page.getTotalPages());
        return mediaPageDTO;
    }

    @Override
    public MediaItemDTO getByIdOrCode(String code) {
        // 1. 查询数据库
        SysFile sysFile = fileService.getByIdOrCode(code);
        if (sysFile == null) {
            return null; // 或抛异常
        }

        // 2. 创建 DTO
        MediaItemDTO dto = new MediaItemDTO();
        BeanUtil.copyProperties(sysFile, dto);
        return dto;
    }

    @Override
    public BatchResult addTag(AddTagRequest addTagRequest) {
        Assert.notNull(addTagRequest);
        Assert.notEmpty(addTagRequest.getFileCodes(), "fileCodes not blank");
        Assert.notNull(addTagRequest.getTagCode(), "tagCode not null");

        // 1. 获取已存在的 file-tag 关系，避免重复添加
        List<FileTagRelation> existingRelations = fileTagRelationDAO.listByTagCode(addTagRequest.getTagCode());
        Set<String> existingFileCodes = existingRelations.stream().map(FileTagRelation::getFileCode).collect(Collectors.toSet());

        // 2. 准备要新增的关系
        List<FileTagRelation> recordsToAdd = addTagRequest.getFileCodes().stream().filter(fileCode -> !existingFileCodes.contains(fileCode)) // 过滤掉已有的
                .map(fileCode -> {
                    FileTagRelation r = new FileTagRelation();
                    r.setFileCode(fileCode);
                    r.setTagCode(addTagRequest.getTagCode());
                    return r;
                }).collect(Collectors.toList());

        // 3. 批量保存
        if (!recordsToAdd.isEmpty()) {
            fileTagRelationDAO.saveAll(recordsToAdd);
        }

        // 4. 返回结果
        BatchResult result = new BatchResult(true);
        result.setMessage("success count =" + recordsToAdd.size() + "fail count=" + (addTagRequest.getFileCodes().size() - recordsToAdd.size()));
        return result;
//        BatchResult result = new BatchResult();
//        List<BatchItemResult> items = addTagRequest.getFileCodes().stream()
//                .map(fileCode -> {
//                    boolean added = !existingFileCodes.contains(fileCode);
//                    if (added) {
//                        FileTagRelation r = new FileTagRelation();
//                        r.setFileCode(fileCode);
//                        r.setTagCode(addTagRequest.getTagCode());
//                        fileTagRelationDAO.save(r);
//                    }
//                    return new BatchItemResult(added, added ? "成功" : "已存在");
//                })
//                .collect(Collectors.toList());
//        result.setSuccess(true);
//        result.setContent(items);
//        result.setMessage("处理完成");
//        return result;

    }

    @Override
    @Transactional
    public BatchResult removeTags(RemoveTagBatchRequest request) {
        BatchResult result = new BatchResult();
        // ------- 参数校验 -------
        if (request == null || request.getRemoveTagRequests() == null) {
            result.setSuccess(false);
            result.setMessage("请求为空");
            return result;
        }
        List<FileTagRelation> fileTagRelations = builtFileTagRelation(request);
        int removed = fileTagRelationDAO.deleteByFileCodesTagCodes(fileTagRelations);
        result.setSuccess(true);
        result.setMessage("删除完成,removed=" + removed);
        return result;
    }

    private List<FileTagRelation> builtFileTagRelation(RemoveTagBatchRequest request) {
        if (request == null || request.getRemoveTagRequests() == null || request.getRemoveTagRequests().isEmpty()) {
            return Collections.emptyList();
        }
        List<FileTagRelation> temp = new ArrayList<>();

        for (RemoveTagRequest item : request.getRemoveTagRequests()) {
            // fileCodes 是 CSV，例如 "a1,a2,a3"
            if (item == null || item.getFileCode() == null || item.getTagCodes() == null || item.getTagCodes().isEmpty())
                continue;

            for (String tagCode : item.getTagCodes()) {
                if (StringTool.isNotBlank(tagCode)) {
                    temp.add(new FileTagRelation(item.getFileCode(), tagCode));
                }
            }
        }
        return temp;
    }

    // 缩略图输出文件夹准备 通过 config来处理， config中 关于默认路径和默认的两个文件
    // 1 遍历 媒体文件（视频和图片） 调用 ffmpeg 生成按一定规则生成缩略图（质量 图大小 视频的什么时刻的缩略图）
    // 2 缩略图文件存储到文件夹 并保存到索引db中
    // 3 索引数据更新到 原视频中的缩略图字段上
    @Override
    public GenerateThumbnailResult generateThumbnails(GenerateThumbnailConfig config) {
        File outputFolder = checkAndPrepareThumbnailFolder(config.getThumbnailRoot());
        config.setThumbnailRoot(outputFolder.getAbsolutePath());

        GenerateThumbnailItemResult generateImageResult = generateThumbnailsImage(config);
        GenerateThumbnailItemResult GenerateVideoResult = generateThumbnailsVideo(config);
        GenerateThumbnailResult generateThumbnailResult = new GenerateThumbnailResult(generateImageResult, GenerateVideoResult);
        return generateThumbnailResult;
    }

    private File checkAndPrepareThumbnailFolder(String thumbnailFullPath) {
        final File rootDir;
        if (StringTool.isBlank(thumbnailFullPath)) {
            rootDir = getThumbnailRoot();
        } else {
            rootDir = FileUtil.requireFileDirectoryExistsOrElseTryCreate(new File(thumbnailFullPath));
        }

        //  隐藏配置文件
        FileUtil.requireFileExistsOrElseTryCreate(new File(rootDir, nomedia));
        FileUtil.requireFileExistsOrElseTryCreate(new File(rootDir, ignore));
        return rootDir;
    }

    @Override
    public CleanThumbnailsResult cleanThumbnails(boolean moveToTrash) {
        List<SysFile> thumbnails = sysFileDAO.listAllByMediaType(THUMBNAIL);
        if (thumbnails == null) {
            return CleanThumbnailsResult.ok();
        }

        File thumbnailRootFile = getThumbnailRoot();

        if (thumbnailRootFile.exists() && thumbnailRootFile.isDirectory()) {
            //  delete
            Map<String, SysFile> thumbnailMap = thumbnails.stream().collect(Collectors.toMap(
                    f -> f.getPath(),  // key=path
                    f -> f,      // value
                    (a, b) -> a    // 解决 key 冲突时返回前者
            ));

            int deleteCount = 0;
            List<File> files = FileUtil.listFileByDir(thumbnailRootFile);
            for (File file : files) {
                String filename = file.getName();
                if (thumbnailMap.get(file.getAbsolutePath()) == null
                        && !Platform.isSystemDefault(filename)
                        && !nomedia.equals(filename)
                        && !ignore.equals(filename)) {

                    if (FileUtil.deleteFile(file, moveToTrash)) {
                        deleteCount = deleteCount + 1;
                    }

                }
            }
            CleanThumbnailsResult result = CleanThumbnailsResult.ok();
            result.setTotal(files.size());
            result.setDeleteCount(deleteCount);
            return result;

        } else {
            return CleanThumbnailsResult.ok();
        }

    }

    private File getThumbnailRoot() {
        return FileUtil.requireFileDirectoryExistsOrElseTryCreate(new File(".", thumbnailsDefaultFolderName));
    }

    private GenerateThumbnailItemResult generateThumbnailsVideo(GenerateThumbnailConfig config) {
        // 1 查询db中的全部视频
        List<SysFile> videos = sysFileDAO.listAllByMediaType(VIDEO);
        GenerateThumbnailItemResult result = new GenerateThumbnailItemResult();
        if (videos == null || videos.isEmpty()) {
            return result;
        }
        result.setTotal(videos.size());
        List<SysFile> updateAll = new LinkedList<>();
        // ffmpeg 去处理视频得到截图
        for (SysFile video : videos) {
            if (video == null) continue;
            boolean doThumbnails = StringTool.isBlank(video.getThumbnail());
            if (doThumbnails || config.isForce()) {
                List<File> files = generateVideoThumbnails(video.getPath(), config.getThumbnailRoot(), Arrays.asList("00:00:05.000", "00:01:00.000"), config.getWidth(), config.isOverwrite());
                List<SysFile> collect = files.stream()
                        .filter(File::exists)
                        .map(file -> SysFile.createSysFile(file, THUMBNAIL, "thumbnail"))
                        .filter(item -> !indexDbExistSameThumbnails(item.getCode()))//
                        .collect(Collectors.toList());
                if (collect == null || collect.isEmpty()) {
                    continue;
                }
                for (SysFile sysFile : collect) {
                    sysFile.setTaskStatus("END");
                }
                boolean saveAll = fileService.saveAll(collect);
                if (saveAll) {
                    final Set<String> thumbnailCodes;
                    if (video.getThumbnail() != null) {
                        thumbnailCodes = Arrays.stream(video.getThumbnail().split(",")).collect(Collectors.toSet());
                    } else {
                        thumbnailCodes = new HashSet<>();
                    }

                    Set<String> thumbnails = collect.stream().map(item -> item.getCode()).collect(Collectors.toSet());
                    thumbnailCodes.addAll(thumbnails);

                    video.setThumbnail(String.join(",", thumbnailCodes));
                    video.setTaskStatus("END");
                    updateAll.add(video);
                }
            }
        }
        if (!updateAll.isEmpty()) sysFileDAO.updateAll(updateAll);
        return result;
    }

    private boolean indexDbExistSameThumbnails(String code) {

        List<SysFile> existRecords = fileService.listByCode(code);
        if (existRecords == null || existRecords.isEmpty()) {
            return false;
        } else {
            for (SysFile existRecord : existRecords) {
                if ((String.valueOf(existRecord.getPath())).contains(thumbnailsDefaultFolderName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private GenerateThumbnailItemResult generateThumbnailsImage(GenerateThumbnailConfig config) {
        List<SysFile> fileList = sysFileDAO.listAllByMediaType("IMAGE");
        GenerateThumbnailItemResult result = new GenerateThumbnailItemResult();
        if (fileList == null || fileList.isEmpty()) {
            return result;
        }
        result.setTotal(fileList.size());
        List<SysFile> updateAll = new LinkedList<>();
        Long imageSkipSize = config.getImageSkipSize();
        for (SysFile file : fileList) {
            if (file == null) continue;
            boolean doThumbnails = false;// StringTool.isBlank(file.getThumbnail());
            if (file.getLength() <= imageSkipSize) {
                doThumbnails = false;
            }

            if (doThumbnails || config.isForce()) {

                File imageThumbnail = generateImageThumbnail(new File(file.getPath()), new File(config.getThumbnailRoot()), config.getWidth(), config.isOverwrite(), "jpg");
                SysFile imageThumbnailSysFile = createSysFile(imageThumbnail, THUMBNAIL, "thumbnail");
                MediaItemDTO byIdOrCode = getByIdOrCode(imageThumbnailSysFile.getCode());
                if (byIdOrCode != null) {
                    //  存在相同的文件了
                    continue;
                }
                imageThumbnailSysFile.setTaskStatus("END");
                Long save = fileService.save(imageThumbnailSysFile);
                if (save != null) {
                    final Set<String> thumbnailCodes;
                    if (file.getThumbnail() != null) {
                        thumbnailCodes = Arrays.stream(file.getThumbnail().split(",")).collect(Collectors.toSet());
                    } else {
                        thumbnailCodes = new HashSet<>();
                    }
                    thumbnailCodes.add(imageThumbnailSysFile.getCode());
                    file.setThumbnail(String.join(",", thumbnailCodes));
                    file.setTaskStatus("END");
                    updateAll.add(file);
                }
            }


        }
        if (!updateAll.isEmpty()) sysFileDAO.updateAll(updateAll);
        return result;
    }


}
