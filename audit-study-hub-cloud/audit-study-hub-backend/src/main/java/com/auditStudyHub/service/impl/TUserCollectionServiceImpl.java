package com.auditStudyHub.service.impl;

import com.auditStudyHub.entity.TUserCollection;
import com.auditStudyHub.mapper.TUserCollectionMapper;
import com.auditStudyHub.service.TUserCollectionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 用户收藏表(TUserCollection)表服务实现类
 *
 * @author Auroral
 * @since 2025-05-06 11:28:51
 */
@Service("tUserCollectionService")
public class TUserCollectionServiceImpl extends ServiceImpl<TUserCollectionMapper, TUserCollection> implements TUserCollectionService {

}

