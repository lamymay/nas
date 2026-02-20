package com.arc.nas.repository.mysql.dao.app;


import com.arc.nas.model.domain.app.media.FileTagRelation;

import java.util.List;
import java.util.Set;

/**
 * 文件操作DAO
 */
public interface FileTagRelationDAO {

    FileTagRelation saveOne(FileTagRelation record);

    boolean saveAll(List<FileTagRelation> records);

    int deleteById(Long id);

    int deleteByIds(List<Long> ids);

    Boolean deleteByFileIds(Set<Long> fileIds);

    Boolean deleteByTagIds(Set<Long> tagIds);

    int deleteByFileIdsTagIds(List<FileTagRelation> fileTagRelations);

    boolean update(FileTagRelation record);

    boolean updateAll(List<FileTagRelation> records);

    FileTagRelation getById(Long id);

    List<FileTagRelation> listAll();

    List<FileTagRelation> listByFileId(Long fileId);

    List<FileTagRelation> listByTagId(Long tagId);

    List<FileTagRelation> listByFileIdAndTagId(Long fileId, Long tagId);
}
