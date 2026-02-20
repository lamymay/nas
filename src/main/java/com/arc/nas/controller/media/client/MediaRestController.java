package com.arc.nas.controller.media.client;

import com.arc.nas.model.dto.app.media.MediaPageDTO;
import com.arc.nas.model.request.app.media.SysFilePageable;
import com.arc.nas.service.app.media.MediaService;
import com.arc.nas.service.app.media.impl.UrlHelper;
import com.arc.nas.service.system.common.SysFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/media")
public class MediaRestController {

    private static final Logger log = LoggerFactory.getLogger(MediaRestController.class);

    private final MediaService mediaService;
    private final SysFileService sysFileService;

    private final UrlHelper urlHelper;

    public MediaRestController(MediaService mediaService, SysFileService sysFileService, UrlHelper urlHelper) {
        this.mediaService = mediaService;
        this.sysFileService = sysFileService;
        this.urlHelper = urlHelper;
    }

    @GetMapping("/home")
    public ResponseEntity<MediaPageDTO> home(
            @RequestParam(defaultValue = "1") Integer pageNumber,
            @RequestParam(defaultValue = "20000") Integer pageSize,
            @RequestParam(required = false) String mediaType,
            @RequestParam(required = false) String orderField,
            @RequestParam(required = false) String orderDirection,
            @RequestParam(required = false) String keyword
    ) {
        SysFilePageable pageable = new SysFilePageable(pageNumber, pageSize);
        pageable.setMediaType(mediaType);
        pageable.setKeyword(keyword);
        pageable.setOrderField(orderField);
        pageable.setOrderDirection(orderDirection);
        return ResponseEntity.ok(mediaService.listPage(pageable));
    }

}
