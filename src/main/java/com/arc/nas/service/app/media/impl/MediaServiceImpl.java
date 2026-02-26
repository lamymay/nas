package com.arc.nas.service.app.media.impl;

import com.arc.nas.model.domain.system.common.SysFile;
import com.arc.nas.model.dto.app.media.MediaItemDTO;
import com.arc.nas.model.dto.app.media.MediaPageDTO;
import com.arc.nas.model.request.app.media.SysFilePageable;
import com.arc.nas.repository.mysql.dao.app.FileTagRelationDAO;
import com.arc.nas.repository.mysql.dao.system.SysFileDAO;
import com.arc.nas.service.app.media.MediaService;
import com.arc.nas.service.system.common.SysFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.arc.nas.model.dto.app.media.MediaItemDTO.covertSysFileToMediaItemDTO;
import static com.arc.nas.service.app.media.impl.MediaFeedService.filter;
import static com.arc.nas.service.app.media.impl.MediaFeedService.getWebSupportVideo;
import static com.arc.util.file.FileUtil.FileUtilConst.VIDEO;

@Service
public class MediaServiceImpl implements MediaService {

    private static final Logger log = LoggerFactory.getLogger(MediaServiceImpl.class);

    private final SysFileService fileService;
    private final SysFileDAO sysFileDAO;
    private final UrlHelper urlHelper;

    public MediaServiceImpl(SysFileService fileService, SysFileDAO sysFileDAO, FileTagRelationDAO fileTagRelationDAO,
                            UrlHelper urlHelper) {
        this.fileService = fileService;
        this.sysFileDAO = sysFileDAO;
        this.urlHelper = urlHelper;
        // 创建 Guava ListeningExecutorService，线程池大小 = CPU 核数
        //this.executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
    }

    @Override
    public MediaPageDTO listPage(SysFilePageable pageable) {
        MediaPageDTO mediaPageDTO = new MediaPageDTO();
        mediaPageDTO.setPageNumber(pageable.getPageNumber());
        mediaPageDTO.setPageSize(pageable.getPageSize());

        Page<SysFile> page = fileService.listPage(pageable);
        if (VIDEO.equals(pageable.getMediaType())) {
            List<SysFile> filter = filter(page.getContent(), getWebSupportVideo());
            List<MediaItemDTO> mediaItemDTOList = covertSysFileToMediaItemDTO(filter, urlHelper.getPrefix());
            mediaPageDTO.setContent(mediaItemDTOList);
            // todo 优化 filter后计数
            mediaPageDTO.setTotalElements(page.getTotalElements());
            mediaPageDTO.setTotalPages(page.getTotalPages());
        }else {
            mediaPageDTO.setContent(covertSysFileToMediaItemDTO(page.getContent(), urlHelper.getPrefix()));
            mediaPageDTO.setTotalElements(page.getTotalElements());
            mediaPageDTO.setTotalPages(page.getTotalPages());
        }

        return mediaPageDTO;
    }

}
