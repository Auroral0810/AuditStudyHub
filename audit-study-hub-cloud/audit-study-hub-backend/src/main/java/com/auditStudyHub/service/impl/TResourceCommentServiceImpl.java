package com.auditStudyHub.service.impl;

import com.auditStudyHub.entity.TResourceComment;
import com.auditStudyHub.mapper.TResourceCommentMapper;
import com.auditStudyHub.service.TResourceCommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 资源评论表(TResourceComment)表服务实现类
 *
 * @author Auroral
 * @since 2025-05-06 11:28:27
 */
@Service("tResourceCommentService")
public class TResourceCommentServiceImpl extends ServiceImpl<TResourceCommentMapper, TResourceComment> implements TResourceCommentService {

}

