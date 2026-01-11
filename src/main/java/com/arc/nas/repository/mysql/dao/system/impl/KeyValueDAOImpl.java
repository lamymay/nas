package com.arc.nas.repository.mysql.dao.system.impl;

import com.arc.nas.model.domain.system.common.KeyValue;
import com.arc.nas.repository.mysql.dao.system.KeyValueDAO;
import com.arc.nas.repository.mysql.mapper.system.KeyValueMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;


@Component
public class KeyValueDAOImpl extends ServiceImpl<KeyValueMapper, KeyValue> implements KeyValueDAO {

    @Override
    public KeyValue saveOne(KeyValue record) {
        if (record == null) throw new RuntimeException("保存数据预期非空");
        return this.save(record) ? record : null;
    }

    @Override
    public boolean saveAll(List<KeyValue> records) {
        if (records == null || records.isEmpty()) throw new RuntimeException("批量保存数据预期非空");
        return this.saveBatch(records, 100);
    }

    @Override
    public boolean update(KeyValue record) {
        if (record == null) throw new RuntimeException("保存数据预期非空");
        if (record.getId() == null) throw new RuntimeException("保存数据ID预期非空");
        return this.updateById(record);
    }

    @Override
    public boolean updateAll(List<KeyValue> records) {
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
    public KeyValue getById(Long id) {
        return this.getBaseMapper().selectById(id);
    }


    @Override
    public List<KeyValue> list(KeyValue query) {
        if (query == null) return Collections.emptyList();
        return this.lambdaQuery()
                .select().list();

    }

    @Override
    public List<KeyValue> listByRange(String range) {
        if (range == null) return Collections.emptyList();
        return this.lambdaQuery()
                .eq(KeyValue::getRange, range)
                .select().list();
    }


    //        LambdaQueryWrapper<KeyValue> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(KeyValue::getKey, key);
    @Override
    public KeyValue getByKey(String key) {
        if (key == null) return null;

        return this.lambdaQuery()
                .eq(KeyValue::getKey, key)
                .select().one();
//        QueryWrapper<KeyValue> queryWrapper = new QueryWrapper<>();
//        queryWrapper.lambda().eq(KeyValue::getKey, key)
//                .select(KeyValue::getId, KeyValue::getCreateTime, KeyValue::getUpdateTime,
//                        KeyValue::getKey, KeyValue::getValue, KeyValue::getRange,
//                        KeyValue::getRemark, KeyValue::getTtl);
//        return this.getOne(queryWrapper);
    }

    @Override
    public IPage<KeyValue> listPage(Map<String, Object> ext) {
        long current = ext.get("current") == null ? 1 : (Long) ext.get("current");
        long size = ext.get("current") == null ? 20 : (Long) ext.get("current");
        return this.lambdaQuery().page(new Page(current, size));
    }

}

