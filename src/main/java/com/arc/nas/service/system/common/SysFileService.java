package com.arc.nas.service.system.common;

import com.arc.nas.model.domain.system.common.SysFile;
import com.arc.nas.model.request.app.media.SysFilePageable;
import com.arc.nas.model.request.app.media.SysFileQuery;
import com.arc.nas.model.request.app.media.BatchItemResult;
import com.arc.nas.model.request.app.media.BatchResult;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * JAVA项目是分层来写的，
 * 这是服务层，目的是处理业务，
 * 文件记录表相关服务
 *
 * @since 2018/12/21
 */
public interface SysFileService {

    //String mediaType;//  业务分类：视频/音频/图片  类型 文件还是图片 VIDEO/IMAGE/FILE/THUMBNAIL  一般来说图片是可以直接预览的,
    String VIDEO = "VIDEO";
    String IMAGE = "IMAGE";
    String FILE = "FILE";
    String THUMBNAIL = "THUMBNAIL";

    Long save(SysFile sysFile);

    //删除文件并且清理
    Boolean deleteById(Long id);

    Boolean deleteByCode(String code);

    Map<String, BatchItemResult> deleteByCodes(String... codes);

//    int deleteByCode(String code);

//    int deleteByRequest(Map<String, Object> map);


    boolean update(SysFile sysFile);

    boolean updateAll(List<SysFile> records);

    BatchResult updateAllByCodes(List<SysFile> records);

    SysFile get(Long id);

    /**
     * 文件持久化并在数据库做记录
     * 注意文件名称保证不相同，不存在重复文件覆盖问题，同时带来一个问题，前端相同文件重复上传造成服务端资源浪费，建议用定时线程去清理无效的重复文件
     *
     * @param file 文件
     * @return 数据库记录凭据--这里返回的文件路径（toDiskPath 唯一），用于查询
     */
    SysFile writeFileToLocalDiskAndCreateDBIndex(MultipartFile file);

    boolean saveAll(List<SysFile> files);

    @Deprecated
    SysFile getById(Long id);

    List<SysFile> listByCode(String code);

    SysFile getByIdOrCode(Object idOrCode);

    /**
     * 分页查询
     *
     * @param pageable pageable
     * @return Page
     */
    Page<SysFile> listPage(SysFilePageable pageable);

    File download(String url);

    SysFile getByName(String name);

    List<SysFile> listAll();

    //    Map<String,List<SysFile>> listAllByMediaTypes(String ...mediaTypes);

    // UI 用
    Map<String, Map<String, SysFile>> listAllByMediaTypes(String... mediaTypes);

    List<SysFile> listLikeDisplayName(String fileName);

    List<SysFile> listAllByMediaType(String thumbnail);

    Map<String, Map<String, SysFile>> listAllByQuery(SysFileQuery pageable);

}
