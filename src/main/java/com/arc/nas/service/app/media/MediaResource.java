package com.arc.nas.service.app.media;

import com.arc.nas.model.domain.app.media.MediaFileResource;
import com.arc.nas.model.request.app.media.*;

import java.util.List;

/**
 * for CMS
 */
public interface MediaResource {

    List<MediaFileResource> listAll();

    List<MediaFileResource> saveAll(String... records);

    int deleteAll();

    int deleteAll(String... records);

    BatchResult addTag(AddTagInner addTagInner);

    BatchResult removeTags(RemoveTagBatchRequest request);

    Integer scan(String... folders);

    void updateHash(boolean force);

    void autoMatchThumbnails();

    GenerateThumbnailResult generateThumbnails(GenerateThumbnailConfig config);

    CleanThumbnailsResult cleanThumbnails(boolean moveToTrash);

}

