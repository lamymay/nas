package com.arc.nas.repository.mysql.dao.app.impl;

import com.arc.nas.model.domain.app.media.FileTagRelation;
import com.arc.nas.repository.mysql.dao.app.FileTagRelationDAO;
import com.arc.nas.repository.mysql.mapper.app.FileTagRelationMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class FileTagRelationDAOImpl extends ServiceImpl<FileTagRelationMapper, FileTagRelation> implements FileTagRelationDAO {

    private static final Logger log = LoggerFactory.getLogger(FileTagRelationDAOImpl.class);

    @Override
    public FileTagRelation saveOne(FileTagRelation record) {
        if (record == null) throw new RuntimeException("保存数据预期非空");
        return this.save(record) ? record : null;
    }

    @Override
    public boolean saveAll(List<FileTagRelation> records) {

        if (records == null || records.isEmpty()) throw new RuntimeException("批量保存数据预期非空");
        return this.saveBatch(records, 100);
    }

    @Override
    public boolean update(FileTagRelation record) {
        if (record == null) throw new RuntimeException("更新数据预期非空");
        boolean update = this.updateById(record);
        return update;
    }

    @Override
    public boolean updateAll(List<FileTagRelation> records) {
        if (records == null || records.isEmpty()) throw new RuntimeException("批量更新数据预期非空");
        return this.updateBatchById(records);

    }

    @Override
    public int deleteById(Long id) {
        return this.getBaseMapper().deleteById(id);

    }

    @Override
    public int deleteByIds(List<Long> ids) {
        return this.getBaseMapper().deleteBatchIds(ids);

    }

    @Override
    public FileTagRelation getById(Long id) {
        return this.getBaseMapper().selectById(id);
    }


    @Override
    public Boolean deleteByFileIds(Set<Long> codes) {
        if (codes == null || codes.isEmpty()) return false;
        return this.lambdaUpdate().in(FileTagRelation::getFileId, codes).remove();

    }

    @Override
    public Boolean deleteByTagIds(Set<Long> codes) {
        if (codes == null || codes.isEmpty()) return false;
        return this.lambdaUpdate().in(FileTagRelation::getTagId, codes).remove();

    }


    @Override
    public List<FileTagRelation> listAll() {
        return this.lambdaQuery().select().list();
    }

    @Override
    public List<FileTagRelation> listByFileId(Long fileId) {
        if (fileId == null) return Collections.emptyList();
        return this.lambdaQuery()
                .eq(FileTagRelation::getFileId, fileId)
                .select().list();

    }

    @Override
    public List<FileTagRelation> listByTagId(Long tagId) {
        if (tagId == null) return Collections.emptyList();
        return this.lambdaQuery()
                .eq(FileTagRelation::getTagId, tagId)
                .select().list();

    }

    @Override
    public List<FileTagRelation> listByFileIdAndTagId(Long fileId, Long tagId) {
        if (fileId == null || tagId == null) return Collections.emptyList();
        return this.lambdaQuery()
                .eq(FileTagRelation::getFileId, fileId)
                .eq(FileTagRelation::getTagId, tagId)
                .select().list();

    }

    @Override
    public int deleteByFileIdsTagIds(List<FileTagRelation> fileTagRelations) {
        if (fileTagRelations == null || fileTagRelations.isEmpty()) return 0;
        int count = 0;
        for (FileTagRelation fileTagRelation : fileTagRelations) {
            this.lambdaUpdate()
                    .eq(FileTagRelation::getFileId, fileTagRelation.getFileId())
                    .eq(FileTagRelation::getTagId, fileTagRelation.getTagId())
                    .remove();
            count = count + 1;
        }
        return count;
    }
}

