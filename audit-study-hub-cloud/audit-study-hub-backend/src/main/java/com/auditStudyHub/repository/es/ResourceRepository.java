package com.auditStudyHub.repository.es;

import com.auditStudyHub.document.ResourceDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * 资源文档仓库
 */
@Repository
public interface ResourceRepository extends ElasticsearchRepository<ResourceDocument, Long> {
    // 默认提供基础的CRUD功能
    // 可以添加自定义查询方法
}
