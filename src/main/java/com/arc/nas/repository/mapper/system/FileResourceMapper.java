package com.arc.nas.repository.mapper.system;

import com.arc.nas.model.domain.system.common.ResourceConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FileResourceMapper extends BaseMapper<ResourceConfig> {

    int deleteByPaths(@Param("paths") List<String> paths);
}
