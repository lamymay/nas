package com.arc.nas.repository.mysql.dao.app.impl;

import com.arc.nas.model.domain.app.media.MediaTag;
import com.arc.nas.model.domain.app.media.MediaTagRequest;
import com.arc.nas.model.request.app.media.BatchItemResult;
import com.arc.nas.model.request.app.media.SysFilePageable;
import com.arc.nas.repository.mysql.dao.app.MediaTagDAO;
import com.arc.nas.repository.mysql.mapper.app.MediaTagMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class MediaTagDAOImpl extends ServiceImpl<MediaTagMapper, MediaTag> implements MediaTagDAO {

    @Override
    public MediaTag saveOne(MediaTag record) {
        if (record == null) throw new RuntimeException("保存数据预期非空");
        return this.save(record) ? record : null;
    }

    @Override
    public boolean saveAll(List<MediaTag> records) {
        if (records == null || records.isEmpty()) throw new RuntimeException("批量保存数据预期非空");
        return this.saveBatch(records, 100);
    }

    @Override
    public boolean update(MediaTag record) {
        if (record == null) throw new RuntimeException("保存数据预期非空");
        if (record.getCode() == null) throw new RuntimeException("保存数据code预期非空");
        return this.updateById(record);
    }

    @Override
    public boolean updateAll(List<MediaTag> records) {
        if (records == null || records.isEmpty()) throw new RuntimeException("批量更新数据预期非空");
        return this.updateBatchById(records);

    }

    @Override
    public int deleteByCode(String id) {
        return this.getBaseMapper().deleteById(id);

    }

    @Override
    public Map<String, BatchItemResult> deleteByCodes(List<String> codes) {
        Map<String, BatchItemResult> resultMap = new HashMap<>();
        if (codes == null || codes.isEmpty()) {
            return resultMap;
        }

        // 删除成功的数量
        int deleted = this.getBaseMapper().deleteBatchIds(codes);

        // 遍历 codes，设置每个 code 的结果
        for (String code : codes) {
            BatchItemResult result = new BatchItemResult();
            if (deleted > 0) {
                // 这里只能粗略判断，如果全部删除数量=codes.size()，全部成功
                // 实际情况可能部分删除失败，需要根据数据库返回精确判断
                result.setSuccess(true);
                result.setMessage("删除成功");
            } else {
                result.setSuccess(false);
                result.setMessage("删除失败或不存在");
            }
            resultMap.put(code, result);
        }

        return resultMap;
    }


    @Override
    public MediaTag getById(Long id) {
        return this.getBaseMapper().selectById(id);
    }


    @Override
    public List<MediaTag> list(MediaTagRequest query) {
        if (query == null) return Collections.emptyList();
        return this.lambdaQuery()
                .like(StringUtils.isNotBlank(query.getDisplayName()), MediaTag::getDisplayName, query.getDisplayName())
                .select().list();

    }


    //        LambdaQueryWrapper<MediaTag> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(MediaTag::getKey, key);
    @Override
    public MediaTag getByCode(String code) {
        if (code == null) return null;

        return this.lambdaQuery()
                .eq(MediaTag::getCode, code)
                .select().one();
//        QueryWrapper<MediaTag> queryWrapper = new QueryWrapper<>();
//        queryWrapper.lambda().eq(MediaTag::getKey, key)
//                .select(MediaTag::getId, MediaTag::getCreateTime, MediaTag::getUpdateTime,
//                        MediaTag::getKey, MediaTag::getValue, MediaTag::getRange,
//                        MediaTag::getRemark, MediaTag::getTtl);
//        return this.getOne(queryWrapper);
    }


    @Override
    public IPage<MediaTag> listPage(SysFilePageable pageable) {
        return this.lambdaQuery()
                .like(StringUtils.isNotBlank(pageable.getKeyword()), MediaTag::getDisplayName, pageable.getKeyword())
//                .orderByDesc(SysFile::getCreateTime)
                .page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageable.getPageNumber(), pageable.getPageSize()));

//                ;

    }

}

