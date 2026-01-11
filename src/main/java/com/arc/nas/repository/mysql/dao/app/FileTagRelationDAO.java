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

    boolean update(FileTagRelation record);

    boolean updateAll(List<FileTagRelation> records);


    int deleteById(Long id);

    int deleteByIds(List<Long> ids);

    int deleteByFileCodesTagCodes(List<FileTagRelation> fileTagRelations);


    FileTagRelation getById(Long id);

    Boolean deleteByFileCodes(Set<String> codes);

    Boolean deleteByTagCodes(Set<String> codes);


    List<FileTagRelation> listAll();

    List<FileTagRelation> listByFileCode(String fileCode);

    List<FileTagRelation> listByTagCode(String tagCode);

    List<FileTagRelation> listByFileCodeAndTagCode(String fileCode, String tagCode);
}
