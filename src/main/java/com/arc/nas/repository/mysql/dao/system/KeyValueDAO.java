package com.arc.nas.repository.mysql.dao.system;

import com.arc.nas.model.domain.system.common.KeyValue;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.Map;

/**
 * DAO封装
 *
 * @author may
 * @since 2021.12.09 11:59 下午
 */
public interface KeyValueDAO {

    KeyValue saveOne(KeyValue record);

    boolean saveAll(List<KeyValue> records);

    boolean update(KeyValue record);

    boolean updateAll(List<KeyValue> records);

    int deleteById(Long id);

    int deleteByIds(List<Long> ids);

    KeyValue getById(Long id);

    KeyValue getByKey(String key);

    List<KeyValue> list(KeyValue query);

    List<KeyValue> listByRange(String range);

    IPage<KeyValue> listPage(Map<String, Object> pageable);
}
