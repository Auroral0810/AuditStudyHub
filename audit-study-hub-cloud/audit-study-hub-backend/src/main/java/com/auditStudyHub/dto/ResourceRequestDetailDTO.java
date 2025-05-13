package com.auditStudyHub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "资源请求详情DTO")
public class ResourceRequestDetailDTO {
    @Schema(description = "请求基本信息")
    private ResourceRequestDTO request;
    
    @Schema(description = "回复列表")
    private List<ResourceRequestReplyDTO> replies;
    
    @Schema(description = "回复总数")
    private Integer replyCount;
}
