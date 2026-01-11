package com.arc.nas.service.app.media;

import com.arc.nas.model.domain.app.media.MediaFileResource;
import com.arc.nas.model.dto.app.media.ScanRequest;

import java.util.List;

public interface MediaResource {

    @Deprecated
    Object setScan(ScanRequest request);

    List<MediaFileResource> listAll();

    List<MediaFileResource> saveAll(String... records);

    int deleteAll();

    int deleteAll(String... records);

}

