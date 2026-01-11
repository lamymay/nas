package com.arc.nas.repository.mysql.dao.app.impl;

import com.arc.nas.model.domain.app.media.MediaFileResource;
import com.arc.nas.model.request.app.media.SysFilePageable;
import com.arc.nas.repository.mysql.dao.app.MediaFileResourceDAO;
import com.arc.nas.repository.mysql.mapper.app.MediaFileResourceMapper;
import com.arc.util.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class MediaFileResourceDAOImpl extends ServiceImpl<MediaFileResourceMapper, MediaFileResource> implements MediaFileResourceDAO {

    @Override
    public MediaFileResource saveOne(MediaFileResource record) {
        if (record == null) throw new RuntimeException("保存数据预期非空");
        return this.save(record) ? record : null;
    }

    @Override
    public List<MediaFileResource> saveAll(List<MediaFileResource> records) {
        if (records == null || records.isEmpty()) throw new RuntimeException("批量保存数据预期非空");
        boolean savedBatch = this.saveBatch(records, 100);
        if (!savedBatch) {
            log.error("error batch insert SysFileFolder records={}" + JSON.toJSONString(records));
        }
        return records;
    }

    @Override
    public boolean update(MediaFileResource record) {
        if (record == null) throw new RuntimeException("保存数据预期非空");
        return this.updateById(record);
    }

    @Override
    public boolean updateAll(List<MediaFileResource> records) {
        if (records == null || records.isEmpty()) throw new RuntimeException("批量更新数据预期非空");
        return this.updateBatchById(records);

    }

    @Override
    public int deleteAll() {
        int deleted = 0;
        for (MediaFileResource mediaFileResource : listAll()) {
            if (mediaFileResource != null) {
                int delete = this.getBaseMapper().deleteById(mediaFileResource.getId());
                deleted = deleted + delete;
            }
        }
        return deleted;
    }

    @Override
    public int deleteByPaths(List<String> paths) {
        if (paths == null || paths.isEmpty()) {
            return 0;
        }
        return   this.getBaseMapper().deleteByPaths(paths);
    }


    @Override
    public MediaFileResource getById(Long id) {
        return this.getBaseMapper().selectById(id);
    }


    @Override
    public List<MediaFileResource> list(MediaFileResource query) {
        if (query == null) return Collections.emptyList();
        return this.lambdaQuery()
                .like(StringUtils.isNotBlank(query.getRemark()), MediaFileResource::getRemark, query.getRemark())
                .select().list();

    }

    @Override
    public List<MediaFileResource> listAll() {
        List<MediaFileResource> list = this.lambdaQuery()
                .orderByDesc(MediaFileResource::getUpdateTime)
                .select().list();
        return list == null ? Collections.emptyList() : list;
    }


    //        LambdaQueryWrapper<SysFileFolder> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(SysFileFolder::getKey, key);


    @Override
    public IPage<MediaFileResource> listPage(SysFilePageable pageable) {
        return this.lambdaQuery()
//                .like(StringUtils.isNotBlank(pageable.getKeyword()), SysFileFolder::getDisplayName, pageable.getKeyword())
//                .orderByDesc(SysFile::getCreateTime)
                .page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageable.getPageNumber(), pageable.getPageSize()));

//                ;

    }


}

