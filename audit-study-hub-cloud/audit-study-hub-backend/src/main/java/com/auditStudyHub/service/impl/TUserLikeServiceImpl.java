package com.auditStudyHub.service.impl;

import com.auditStudyHub.entity.TUserLike;
import com.auditStudyHub.mapper.TUserLikeMapper;
import com.auditStudyHub.service.TUserLikeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 用户点赞表(TUserLike)表服务实现类
 *
 * @author Auroral
 * @since 2025-05-06 11:29:04
 */
@Service("tUserLikeService")
public class TUserLikeServiceImpl extends ServiceImpl<TUserLikeMapper, TUserLike> implements TUserLikeService {

}

