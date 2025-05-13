package com.auditStudyHub.repository.es;

import com.auditStudyHub.document.ResourceRequestDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceRequestRepository extends ElasticsearchRepository<ResourceRequestDocument, Long> {
}
