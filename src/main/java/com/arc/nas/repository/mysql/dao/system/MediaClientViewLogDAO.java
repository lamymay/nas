package com.arc.nas.repository.mysql.dao.system;

import com.arc.nas.model.domain.app.media.MediaClientViewLog;

import java.util.List;


public interface MediaClientViewLogDAO {

    MediaClientViewLog saveOrUpdateOne(MediaClientViewLog record);

//    MediaClientViewLog saveOne(MediaClientViewLog record);

//    boolean saveAll(List<MediaClientViewLog> records);

    boolean update(MediaClientViewLog record);

    boolean updateAll(List<MediaClientViewLog> records);

//    boolean updateAllByCodes(List<MediaClientViewLog> records);

    boolean deleteById(Long id);

    int deleteByIds(List<Long> ids);

    MediaClientViewLog getById(Long id);

    List<MediaClientViewLog> listAllByQuery(MediaClientViewLog query);

    List<MediaClientViewLog> listAll();

    MediaClientViewLog getByClientCodeAndFileCode(String clientCode, String fileCode);

}
