package com.arc.nas.repository.dao.system;

import com.arc.nas.model.domain.system.common.ClientViewLog;

import java.util.List;


public interface ClientViewLogDAO {

    ClientViewLog saveOrUpdateOne(ClientViewLog record);

//    MediaClientViewLog saveOne(MediaClientViewLog record);

//    boolean saveAll(List<MediaClientViewLog> records);

    boolean update(ClientViewLog record);

    boolean updateAll(List<ClientViewLog> records);

//    boolean updateAllByCodes(List<MediaClientViewLog> records);

    boolean deleteById(Long id);

    int deleteByIds(List<Long> ids);

    ClientViewLog getById(Long id);

    List<ClientViewLog> listAllByQuery(ClientViewLog query);

    List<ClientViewLog> listAll();

    ClientViewLog getByClientCodeAndFileCode(String clientCode, String fileCode);

}
