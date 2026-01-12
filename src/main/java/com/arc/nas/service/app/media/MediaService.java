package com.arc.nas.service.app.media;

import com.arc.nas.model.dto.app.media.MediaItemDTO;
import com.arc.nas.model.dto.app.media.MediaPageDTO;
import com.arc.nas.model.request.app.media.*;

public interface MediaService {

    Integer scan(String... folders);

    void updateHash(boolean force);

    void autoMatchThumbnails();

    MediaPageDTO listPage(SysFilePageable pageable);

    MediaItemDTO getByIdOrCode(String code);

    BatchResult addTag(AddTagRequest addTagRequest);

    BatchResult removeTags(RemoveTagBatchRequest request);

    GenerateThumbnailResult generateThumbnails(GenerateThumbnailConfig config);

    CleanThumbnailsResult cleanThumbnails(boolean moveToTrash);
}

