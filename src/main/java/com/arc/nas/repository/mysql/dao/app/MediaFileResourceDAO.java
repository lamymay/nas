package com.arc.nas.repository.mysql.dao.app;

import com.arc.nas.model.domain.app.media.MediaFileResource;
import com.arc.nas.model.request.app.media.SysFilePageable;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * DAO封装
 *
 * @author may
 * @since 2021.12.09 11:59 下午
 */
public interface MediaFileResourceDAO {

    MediaFileResource saveOne(MediaFileResource record);

    List<MediaFileResource> saveAll(List<MediaFileResource> records);

    boolean update(MediaFileResource record);

    boolean updateAll(List<MediaFileResource> records);

    int deleteAll();

    int deleteByPaths(List<String> paths);

    MediaFileResource getById(Long id);

    List<MediaFileResource> list(MediaFileResource query);

    List<MediaFileResource> listAll();


    IPage<MediaFileResource> listPage(SysFilePageable pageable);


}
