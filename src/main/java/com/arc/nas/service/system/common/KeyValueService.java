package com.arc.nas.service.system.common;

import com.arc.nas.model.domain.system.common.KeyValue;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.Map;

/**
 * @since 2020/4/16 23:31
 */
public interface KeyValueService {

    KeyValue save(KeyValue keyValue);

    boolean update(KeyValue record);

    int delete(Long id);

    KeyValue get(Long id);

    IPage<KeyValue> listPage(Map<String, Object> pageable);

    /**
     * 批量保存异步化--线程池
     *
     * @param kvList data
     */
    boolean saveAll(List<KeyValue> kvList);

    /**
     * 批量查询
     *
     * @param keyValue 查询一个列表的查询参数
     * @return kv的列表
     */
    List<KeyValue> list(KeyValue keyValue);

    List<KeyValue> listByRange(String range);

    //List<Map<String, Object>> listByType(List<String> types);
}
