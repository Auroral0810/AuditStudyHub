package com.auditStudyHub.service.impl;

import com.auditStudyHub.entity.TUserDownload;
import com.auditStudyHub.mapper.TUserDownloadMapper;
import com.auditStudyHub.service.TUserDownloadService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 用户下载表(TUserDownload)表服务实现类
 *
 * @author Auroral
 * @since 2025-05-06 11:28:56
 */
@Service("tUserDownloadService")
public class TUserDownloadServiceImpl extends ServiceImpl<TUserDownloadMapper, TUserDownload> implements TUserDownloadService {

}

