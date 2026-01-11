package com.arc.nas.repository.mysql.mapper.app;

import com.arc.nas.model.domain.app.media.MediaFileResource;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MediaFileResourceMapper extends BaseMapper<MediaFileResource> {
    int deleteByPaths(@Param("paths") List<String> paths);
}
