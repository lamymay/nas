package com.arc.nas.service.app.media.impl;

import com.arc.nas.model.domain.app.media.MediaFileResource;
import com.arc.nas.model.dto.app.media.ScanRequest;
import com.arc.nas.repository.mysql.dao.app.MediaFileResourceDAO;
import com.arc.nas.service.app.media.MediaResource;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MediaResourceImpl implements MediaResource {

    static final String MEDIA_RESOURCE_FLAG = "MediaResource";

    private final MediaFileResourceDAO mediaFileResourceDAO;

    public MediaResourceImpl(MediaFileResourceDAO mediaFileResourceDAO) {
        this.mediaFileResourceDAO = mediaFileResourceDAO;
    }

    public static String formatCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    @Override
    public Object setScan(ScanRequest request) {
        // 1. 快速失败判断
        if (request == null || request.getFolders() == null || request.getFolders().length == 0) {
            return null;
        }

//        // 2. 获取去重后的待插入目录
//        Set<String> folderSet = new HashSet<>(Arrays.asList(request.getFolders()));
//
//        // 3. 获取已存在的 Key（只取出 Key，减少内存占用）
//        List<KeyValue> existRows = keyValueService.listByRange(MEDIA_RESOURCE_FLAG);
//        Set<String> existKeys = (existRows == null) ? Collections.emptySet() :
//                existRows.stream().map(KeyValue::getKey).collect(Collectors.toSet());
//
//        // 4. 过滤出真正需要插入的行 (不在 existKeys 中的)
//        String now = formatCurrentDate();
//        List<KeyValue> executeInsertRows = folderSet.stream()
//                .filter(folder -> !existKeys.contains(folder))
//                .map(folder -> new KeyValue(folder, now, MEDIA_RESOURCE_FLAG))
//                .collect(Collectors.toList());
//
//        // 5. 执行插入并返回
//        if (!executeInsertRows.isEmpty()) {
//            return keyValueService.saveAll(executeInsertRows);
//        }
        return false;
    }

    @Override
    public List<MediaFileResource> listAll() {
        List<MediaFileResource> folders = mediaFileResourceDAO.listAll();
        return folders == null ? new ArrayList<>() : folders;
    }

    @Override
    public List<MediaFileResource> saveAll(String... needInsertRows) {
        if (needInsertRows == null || needInsertRows.length == 0) return Collections.emptyList();

        List<MediaFileResource> existRows = listAll();
        Set<String> existKeys = (existRows == null) ? Collections.emptySet() :
                existRows.stream().map(MediaFileResource::getPath).collect(Collectors.toSet());

        List<MediaFileResource> collect = new ArrayList<>();
        for (String insertPath : needInsertRows) {
            if (insertPath == null) continue;
            if (existKeys.contains(insertPath)) continue;
            collect.add(new MediaFileResource(insertPath, new Date()));

        }
        if (collect.isEmpty()) {
            return Collections.emptyList();
        }
        List<MediaFileResource> mediaFileResources = mediaFileResourceDAO.saveAll(collect);
        return mediaFileResources;
    }

    @Override
    public int deleteAll() {
        return mediaFileResourceDAO.deleteAll();
    }

    @Override
    public int deleteAll(String... records) {
        if(records==null||records.length==0) return 0;
        // 直接转为 List，一步到位
        return mediaFileResourceDAO.deleteByPaths(Arrays.asList(records));
    }
}
