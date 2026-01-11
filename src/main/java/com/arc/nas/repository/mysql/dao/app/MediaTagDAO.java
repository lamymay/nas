package com.arc.nas.repository.mysql.dao.app;

import com.arc.nas.model.domain.app.media.MediaTag;
import com.arc.nas.model.domain.app.media.MediaTagRequest;
import com.arc.nas.model.request.app.media.SysFilePageable;
import com.arc.nas.model.request.app.media.BatchItemResult;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.Map;

/**
 * DAO封装
 *
 * @author may
 * @since 2021.12.09 11:59 下午
 */
public interface MediaTagDAO {

    MediaTag saveOne(MediaTag record);

    boolean saveAll(List<MediaTag> records);

    boolean update(MediaTag record);

    boolean updateAll(List<MediaTag> records);

    int deleteByCode(String code);

    Map<String, BatchItemResult> deleteByCodes(List<String> codes);

    MediaTag getById(Long id);

    MediaTag getByCode(String code);

    List<MediaTag> list(MediaTagRequest query);


    IPage<MediaTag> listPage(SysFilePageable pageable);


}
