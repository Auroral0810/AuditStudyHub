package com.auditStudyHub.service.impl;

import com.auditStudyHub.entity.TResource;
import com.auditStudyHub.mapper.TResourceMapper;
import com.auditStudyHub.service.TResourceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 资源表(TResource)表服务实现类
 *
 * @author Auroral
 * @since 2025-05-06 11:28:21
 */
@Service("tResourceService")
public class TResourceServiceImpl extends ServiceImpl<TResourceMapper, TResource> implements TResourceService {

}

