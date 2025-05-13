package com.auditStudyHub.service.impl;

import com.auditStudyHub.entity.TResourceRequest;
import com.auditStudyHub.mapper.TResourceRequestMapper;
import com.auditStudyHub.service.TResourceRequestService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 资源求助表(TResourceRequest)表服务实现类
 *
 * @author Auroral
 * @since 2025-05-06 11:28:34
 */
@Service("tResourceRequestService")
public class TResourceRequestServiceImpl extends ServiceImpl<TResourceRequestMapper, TResourceRequest> implements TResourceRequestService {

}

