package com.arc.nas.repository.mysql.dao.system.impl;

import com.arc.nas.model.domain.system.common.SysFile;
import com.arc.nas.model.request.app.media.SysFilePageable;
import com.arc.nas.model.request.app.media.SysFileQuery;
import com.arc.nas.repository.mysql.mapper.system.SysFileMapper;
import com.arc.nas.service.system.common.SysFileDAO;
import com.arc.util.StringTool;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.arc.nas.model.request.DefaultPageParameter.DEFAULT_DB_START_PAGE;
import static com.arc.nas.model.request.DefaultPageParameter.DEFAULT_PAGE_SIZE;


@Component
public class SysFileDAOImpl extends ServiceImpl<SysFileMapper, SysFile> implements SysFileDAO {

    private static final Logger log = LoggerFactory.getLogger(SysFileDAOImpl.class);


    @Override
    public SysFile saveOne(SysFile record) {
        if (record == null) throw new RuntimeException("保存数据预期非空");
        return this.save(record) ? record : null;
    }

    @Override
    public boolean saveAll(List<SysFile> records) {
        StopWatch watch = new StopWatch();

        watch.start("数据入库前参数转换map");
        if (records == null || records.isEmpty()) throw new RuntimeException("批量保存数据预期非空");
        Map<String, SysFile> fileMap = records.stream().collect(Collectors.toMap(SysFile::getPath, Function.identity(), (oldValue, newValue) -> newValue));
        watch.stop();

        // 查一下db看下是否重复
        watch.start("数据入库前参数重复性检查-queryAll");
        List<SysFile> sysFileList = listAll();
        watch.stop();

        watch.start("数据入库前参数重复性检查-queryAll-转换为pathList");
        List<String> existPathList = sysFileList.stream().map(SysFile::getPath).collect(Collectors.toList());
        watch.stop();

        watch.start("数据入库前参数重复性检查-筛选出path不相同的");
        LinkedList<SysFile> sysFiles = new LinkedList<>();
        for (String item : fileMap.keySet()) {
            if (!existPathList.contains(item)) {
                sysFiles.add(fileMap.get(item));
            } else {
                log.info("重复性检查-path相同={}", item);
            }
        }
        watch.stop();
        watch.start("数据批量入库-saveAll 实际操作耗时，数量" + sysFiles.size());
        if (sysFiles == null || sysFiles.isEmpty()) {
            watch.stop();
            return true;
        }
        boolean saved = this.saveBatch(sysFiles, 1000);
        watch.stop();
        System.out.println(watch.prettyPrint());
        return saved;
    }

    @Override
    public boolean update(SysFile record) {
        if (record == null) throw new RuntimeException("更新数据预期非空");
        if (record.getId() == null) throw new RuntimeException("更新数据ID预期非空");
        record.setUpdateTime(new Date());
        boolean update = this.updateById(record);
        return update;
    }

    @Override
    public boolean updateAll(List<SysFile> records) {
        if (records == null || records.isEmpty()) throw new RuntimeException("批量更新数据预期非空");
        return this.updateBatchById(records);

    }

    @Override
    public boolean updateAllByCodes(List<SysFile> records) {
        if (records == null || records.isEmpty()) throw new RuntimeException("批量更新数据预期非空");
        for (SysFile record : records) {
            if (record.getCode() == null) throw new RuntimeException("对象 code 不能为空: " + record);
            UpdateWrapper<SysFile> wrapper = new UpdateWrapper<>();
            wrapper.eq("code", record.getCode());
            this.update(record, wrapper); // 根据 code 更新
        }
        return true;
    }

    @Override
    public boolean deleteById(Long id) {
        return this.getBaseMapper().deleteById(id) != 0;
    }

    @Override
    public int deleteByIds(List<Long> ids) {

        return this.getBaseMapper().deleteBatchIds(ids);

    }

    @Override
    public SysFile getById(Long id) {
        return this.getBaseMapper().selectById(id);
    }

    @Override
    public SysFile getByCode(String code) {
        return this.lambdaQuery().eq(code != null, SysFile::getCode, code).select().one();
    }

    @Override
    public List<SysFile> listByCode(String code) {
        return this.lambdaQuery().eq(code != null, SysFile::getCode, code).select().list();
    }

    @Override
    public List<SysFile> getByHash(String hash) {
        return this.lambdaQuery().eq(hash != null, SysFile::getHash, hash).select().list();
    }

    @Override
    public List<SysFile> listAllByMediaType(String mediaType) {
        return this.lambdaQuery().eq(mediaType != null, SysFile::getMediaType, mediaType).select().list();
    }


    @Override
    public List<SysFile> listAllByCodes(Set<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return Collections.emptyList();
        }

        return this.lambdaQuery()
                .in(SysFile::getCode, codes)
                .list();
    }

    @Override
    public List<SysFile> listAllByMediaTypes(Collection<String> mediaTypes) {
        if (mediaTypes == null || mediaTypes.isEmpty()) {
            return Collections.emptyList();
        }

        return this.lambdaQuery()
                .in(SysFile::getMediaType, mediaTypes)
                .list();
    }

    @Override
    public Boolean deleteByCodes(Set<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return false;
        }

        return this.lambdaUpdate()
                .in(SysFile::getCode, codes)
                .remove();

    }

    @Override
    public SysFile getByHashAndName(String hash, String name) {
        return this.lambdaQuery()
                .eq(hash != null, SysFile::getHash, hash).select()
                .eq(name != null, SysFile::getOriginalName, name).select()
                .one();

    }

    @Override
    public SysFile getByIdOrCode(String idOrCode) {
        if (StringUtils.isNumeric(idOrCode)) {
            Long asId = Long.valueOf(idOrCode);
            return getById(asId);
        } else {
            return getByCode(idOrCode);
        }
    }

    @Override
    public List<SysFile> list(SysFile query) {
        if (query == null) return Collections.emptyList();
        return this.lambdaQuery().eq(StringUtils.isNotBlank(query.getCode()), SysFile::getCode, query.getCode()).eq(StringUtils.isNotBlank(query.getOriginalName()), SysFile::getOriginalName, query.getOriginalName()).eq((query.getStatus() != null && query.getStatus() > 0), SysFile::getStatus, query.getStatus())

                .select().list();

    }
//     优化 分页查询 与 搜索

    @Override
    public long count(SysFilePageable query) {
        return this.lambdaQuery()
                .like(StringUtils.isNotBlank(query.getKeyword()), SysFile::getDisplayName, query.getKeyword())
                .eq(StringTool.isNotBlank(query.getMediaType()), SysFile::getMediaType, query.getMediaType())
                .count();
    }

    @Override
    public org.springframework.data.domain.Page<SysFile> listPage(SysFilePageable pageable) {
        int offset = pageable.getPageNumber() < 1 ? DEFAULT_DB_START_PAGE : pageable.getPageNumber() - 1;
        int size = pageable.getPageSize() < 1 ? DEFAULT_PAGE_SIZE : pageable.getPageSize();
        pageable.setOffset(offset);
        pageable.setPageSize(size);

        long total = count(pageable);
        final List<SysFile> records;
        if (total > 0) {
            records = baseMapper.listPageByQuery(pageable);
        } else {
            records = Collections.emptyList();
        }
        return new PageImpl<>(records, pageable, total);
    }


    @Override
    public List<SysFile> listAllByQuery(SysFileQuery query) {
        return this.lambdaQuery()
                .like(StringUtils.isNotBlank(query.getKeyword()), SysFile::getDisplayName, query.getKeyword())
                .in(query.getMediaTypes() != null, SysFile::getMediaType, query.getMediaTypes())
//                .orderByDesc(SysFile::getId)
                .list();

    }

    @Override
    public List<SysFile> listAll() {
        return this.lambdaQuery().select().list();
    }

    @Override
    public List<SysFile> listAllByStatus(int status) {
        return this.lambdaQuery().eq(SysFile::getStatus, status).select().list();
    }

    public SysFile getByIdOrCode(Object idOrCode) {
        return getByIdOrCode(String.valueOf(idOrCode));
    }

    @Override
    public SysFile getByName(String name) {
        return this.lambdaQuery().eq(name != null, SysFile::getDisplayName, name).select().one();
    }

    @Override
    public List<SysFile> listLikeDisplayName(String fileName) {
        return this.lambdaQuery().likeLeft(StringUtils.isNotBlank(fileName), SysFile::getDisplayName, fileName).select().list();
    }

    @Override
    public long countByStatus(Integer status) {
        if (status != null) {
            return this.lambdaQuery().eq(SysFile::getStatus, status).count();
        } else {
            return this.lambdaQuery().count();
        }

    }

    @Override
    public List<SysFile> listFilesByMediaTypesAndTags(SysFileQuery query) {
        return this.baseMapper.listFilesByMediaTypesAndTags(query);
    }


}

