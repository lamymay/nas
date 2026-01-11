package com.arc.nas.repository.mysql.mapper.system;

import com.arc.nas.model.domain.system.common.SysFile;
import com.arc.nas.model.request.app.media.SysFilePageable;
import com.arc.nas.model.request.app.media.SysFileQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * 持久层
 */
public interface SysFileMapper extends BaseMapper<SysFile> {

    List<SysFile> listPageByQuery(SysFilePageable pageable);

    List<SysFile> listFilesByMediaTypesAndTags(SysFileQuery query);

}
