package com.arc.nas.service.app.media;

import com.arc.nas.model.dto.app.media.MediaPageDTO;
import com.arc.nas.model.request.app.media.SysFilePageable;

/**
 * for client
 */
public interface MediaService {

    MediaPageDTO listPage(SysFilePageable pageable);


}

