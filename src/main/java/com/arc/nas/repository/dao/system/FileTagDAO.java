package com.arc.nas.repository.dao.system;

import com.arc.nas.model.domain.system.common.FileTag;
import com.arc.nas.model.request.MediaTagRequest;
import com.arc.nas.model.request.app.media.BatchItemResult;
import com.arc.nas.model.request.app.media.SysFilePageable;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.Map;

/**
 * DAO封装
 */
public interface FileTagDAO {

    FileTag saveOne(FileTag record);

    boolean saveAll(List<FileTag> records);

    boolean update(FileTag record);

    boolean updateAll(List<FileTag> records);

    int deleteByCode(String code);

    Map<String, BatchItemResult> deleteByCodes(List<String> codes);

    FileTag getById(Long id);

    List<FileTag> list(MediaTagRequest query);


    IPage<FileTag> listPage(SysFilePageable pageable);


}
