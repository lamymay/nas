package com.arc.nas.repository.mysql.dao.app.impl;

import com.arc.nas.model.domain.app.media.FileTagRelation;
import com.arc.nas.repository.mysql.dao.app.FileTagRelationDAO;
import com.arc.nas.repository.mysql.mapper.app.FileTagRelationMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;


@Component
public class FileTagRelationDAOImpl extends ServiceImpl<FileTagRelationMapper, FileTagRelation> implements FileTagRelationDAO {

    final static String protocol = "http://";
    private static final Logger log = LoggerFactory.getLogger(FileTagRelationDAOImpl.class);

    @Value("${spring.profiles:local}")
    private String profile;

    @Value("${server.port:80}")
    private int port;

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    public static void main(String[] args) {
        System.out.println(4 * 1024);
    }

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
    public Boolean deleteByFileCodes(Set<String> codes) {
        if (codes == null || codes.isEmpty()) return false;
        return this.lambdaUpdate().in(FileTagRelation::getFileCode, codes).remove();

    }

    @Override
    public Boolean deleteByTagCodes(Set<String> codes) {
        if (codes == null || codes.isEmpty()) return false;
        return this.lambdaUpdate().in(FileTagRelation::getTagCode, codes).remove();

    }


    @Override
    public List<FileTagRelation> listAll() {
        return this.lambdaQuery().select().list();
    }

    @Override
    public List<FileTagRelation> listByFileCode(String fileCode) {
        if (fileCode == null) return Collections.emptyList();
        return this.lambdaQuery()
                .eq(FileTagRelation::getFileCode, fileCode)
                .select().list();

    }

    @Override
    public List<FileTagRelation> listByTagCode(String tagCode) {
        if (tagCode == null || StringUtils.isNotBlank(tagCode)) return Collections.emptyList();
        return this.lambdaQuery()
                .eq(FileTagRelation::getTagCode, tagCode)
                .select().list();

    }

    @Override
    public List<FileTagRelation> listByFileCodeAndTagCode(String fileCode, String tagCode) {
        if (fileCode == null || tagCode == null) return Collections.emptyList();
        return this.lambdaQuery()
                .eq(FileTagRelation::getFileCode, fileCode)
                .eq(FileTagRelation::getTagCode, tagCode)
                .select().list();

    }

    @Override
    public int deleteByFileCodesTagCodes(List<FileTagRelation> fileTagRelations) {
        if (fileTagRelations == null || fileTagRelations.isEmpty()) return 0;
        int count = 0;
        for (FileTagRelation fileTagRelation : fileTagRelations) {
            this.lambdaUpdate()
                    .eq(FileTagRelation::getFileCode, fileTagRelation.getFileCode())
                    .eq(FileTagRelation::getTagCode, fileTagRelation.getTagCode())
                    .remove();
            count = count + 1;
        }
        return count;
    }
}

