package com.arc.nas.service.app.media.impl;

import com.arc.nas.model.domain.system.common.SysFile;
import com.arc.nas.model.dto.app.media.MediaFeedDTO;
import com.arc.nas.model.request.app.media.FeedQuery;
import com.arc.nas.model.request.app.media.SysFileQuery;
import com.arc.nas.repository.mysql.dao.app.FileTagRelationDAO;
import com.arc.nas.repository.mysql.dao.system.MediaClientViewLogDAO;
import com.arc.nas.repository.mysql.dao.system.SysFileDAO;
import com.arc.nas.service.system.common.SysFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class MediaFeedService {

    private static final Logger log = LoggerFactory.getLogger(MediaFeedService.class);
    private final SysFileService fileService;
    private final SysFileDAO sysFileDAO;
    private final FileTagRelationDAO fileTagRelationDAO;
    private final MediaClientViewLogDAO mediaClientViewLogDAO;
    private final UrlHelper urlHelper;

    public MediaFeedService(SysFileService fileService, SysFileDAO sysFileDAO, FileTagRelationDAO fileTagRelationDAO,
                            UrlHelper urlHelper,
                            MediaClientViewLogDAO mediaClientViewLogDAO) {
        this.fileService = fileService;
        this.sysFileDAO = sysFileDAO;
        this.fileTagRelationDAO = fileTagRelationDAO;
        this.urlHelper = urlHelper;
        this.mediaClientViewLogDAO = mediaClientViewLogDAO;
    }

    public MediaFeedDTO feed(FeedQuery query) {
        // 获取数据源
        SysFileQuery sysFileQuery = new SysFileQuery();
        sysFileQuery.setMediaTypes(Set.of(query.getContentType()));
        List<SysFile> sysFiles = fileService.listAllByQuery(sysFileQuery);

        // mock 推荐算法

//        if(mediaClientViewLogDAO)

        // 结果聚合

        // 结果json 组装
        MediaFeedDTO build = MediaFeedDTO.build(sysFiles, urlHelper.getPrefix());
        build.setCursor(query.getCursor()+build.getTotalElements());
        build.setServerTime(query.getStep());
        return build;
    }


}
