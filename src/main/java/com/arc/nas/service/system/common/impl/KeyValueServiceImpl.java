package com.arc.nas.service.system.common.impl;

import com.arc.nas.model.domain.system.common.KeyValue;
import com.arc.nas.repository.mysql.dao.system.KeyValueDAO;
import com.arc.nas.service.system.common.KeyValueService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @since 2020/4/16 23:36
 */
@Service

public class KeyValueServiceImpl implements KeyValueService {

    @Resource
    private KeyValueDAO keyValueDAO;

    @Override
    public KeyValue save(KeyValue keyValue) {
        return keyValueDAO.saveOne(keyValue);
    }

    @Override
    public boolean update(KeyValue record) {
        return keyValueDAO.update(record);

    }

    @Override
    public int delete(Long id) {
        return keyValueDAO.deleteById(id);
    }

    @Override
    public KeyValue get(Long id) {
        return keyValueDAO.getById(id);
    }

    @Override
    public IPage<KeyValue> listPage(Map<String, Object> pageable) {
        return keyValueDAO.listPage(pageable);
    }

    @Override
    public boolean saveAll(List<KeyValue> kvList) {
        return keyValueDAO.saveAll(kvList);
    }

    /**
     * 批量查询
     *
     * @param keyValue 查询一个列表的查询参数
     * @return kv的列表
     */
    @Override
    public List<KeyValue> list(KeyValue keyValue) {
        return keyValueDAO.list(keyValue);
    }

    @Override
    public List<KeyValue> listByRange(String range) {
        return keyValueDAO.listByRange(range);

    }
}
