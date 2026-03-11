package com.arc.nas.repository.dao.system;

import com.arc.nas.model.domain.system.common.ResourceConfig;
import com.arc.nas.model.request.app.media.SysFilePageable;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * DAO封装
 */
public interface FileResourceDAO {

    ResourceConfig saveOne(ResourceConfig record);

    List<ResourceConfig> saveAll(List<ResourceConfig> records);

    boolean update(ResourceConfig record);

    boolean updateAll(List<ResourceConfig> records);

    int deleteAll();

    int deleteByPaths(List<String> paths);

    ResourceConfig getById(Long id);

    List<ResourceConfig> list(ResourceConfig query);

    List<ResourceConfig> listAll();


    IPage<ResourceConfig> listPage(SysFilePageable pageable);


}
