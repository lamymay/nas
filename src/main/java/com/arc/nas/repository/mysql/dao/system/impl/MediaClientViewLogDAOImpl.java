package com.arc.nas.repository.mysql.dao.system.impl;

import com.arc.nas.model.domain.app.media.MediaClientViewLog;
import com.arc.nas.repository.mysql.dao.system.MediaClientViewLogDAO;
import com.arc.nas.repository.mysql.mapper.system.MediaClientViewLogMapper;
import com.arc.util.Assert;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MediaClientViewLogDAOImpl extends ServiceImpl<MediaClientViewLogMapper, MediaClientViewLog>
        implements MediaClientViewLogDAO {

    private static final Logger log = LoggerFactory.getLogger(MediaClientViewLogDAOImpl.class);

    @Override
    public synchronized MediaClientViewLog saveOrUpdateOne(MediaClientViewLog record) {
        if (record == null) throw new RuntimeException("保存数据预期非空");
        MediaClientViewLog exist = getByClientCodeAndFileCode(record.getClientCode(), record.getFileCode());
        if (exist == null) {
            return this.save(record) ? record : null;
        } else {
            exist.setViewTime(record.getViewTime());
            update(exist);
            return exist;
        }
    }
//
//    @Override
//    public MediaClientViewLog saveOne(MediaClientViewLog record) {
//        if (record == null) throw new RuntimeException("保存数据预期非空");
//        MediaClientViewLog exist = getByClientCodeAndFileCode(record.getClientCode(), record.getFileCode());
//        if (exist == null) {
//            return this.save(record) ? record : null;
//        } else {
//            return exist;
//        }
//    }
//
//    @Override
//    public boolean saveAll(List<MediaClientViewLog> records) {
//        for (MediaClientViewLog record : records) {
//            saveOne(record);
//        }
//        return true;
//    }

    @Override
    public boolean update(MediaClientViewLog record) {
        if (record == null) throw new RuntimeException("更新数据预期非空");
        if (record.getId() == null) throw new RuntimeException("更新数据的id预期非空");
        return this.updateById(record);
    }

    @Override
    public boolean updateAll(List<MediaClientViewLog> records) {
        if (records == null || records.isEmpty()) throw new RuntimeException("批量更新数据预期非空");
        return this.updateBatchById(records);

    }

//    @Override
//    public boolean updateAllByCodes(List<MediaClientViewLog> records) {
//        if (records == null || records.isEmpty()) throw new RuntimeException("批量更新数据预期非空");
//        for (MediaClientViewLog record : records) {
//            if (record.getCode() == null) throw new RuntimeException("对象 code 不能为空: " + record);
//            UpdateWrapper<MediaClientViewLog> wrapper = new UpdateWrapper<>();
//            wrapper.eq("code", record.getCode());
//            this.update(record, wrapper); // 根据 code 更新
//        }
//        return true;
//    }

    @Override
    public boolean deleteById(Long id) {
        return this.getBaseMapper().deleteById(id) != 0;
    }

    @Override
    public int deleteByIds(List<Long> ids) {

        return this.getBaseMapper().deleteBatchIds(ids);

    }

    @Override
    public MediaClientViewLog getById(Long id) {
        return this.getBaseMapper().selectById(id);
    }

    @Override
    public List<MediaClientViewLog> listAllByQuery(MediaClientViewLog   query) {
        return this.lambdaQuery()
                .eq(query.getClientCode() != null, MediaClientViewLog::getClientCode, query.getClientCode())
                .eq(query.getFileCode() != null, MediaClientViewLog::getFileCode, query.getFileCode())
                .list();

    }

    @Override
    public List<MediaClientViewLog> listAll() {
        return this.lambdaQuery().select().list();
    }

    @Override
    public MediaClientViewLog getByClientCodeAndFileCode(String clientCode, String fileCode) {
        Assert.notNull(clientCode,"clientCode not allow null");
        Assert.notNull(fileCode,"fileCode not allow null");

        try {
           return this.lambdaQuery()
                .eq(clientCode != null, MediaClientViewLog::getClientCode, clientCode)
                .eq(fileCode != null, MediaClientViewLog::getFileCode, fileCode)
                .select().one();

        } catch (Exception exception) {
            log.error("getByClientCodeAndFileCode",exception);
            throw exception;
        }
    }


}

