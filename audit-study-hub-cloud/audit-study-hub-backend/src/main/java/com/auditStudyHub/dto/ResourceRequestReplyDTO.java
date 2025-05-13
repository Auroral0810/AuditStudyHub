package com.auditStudyHub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 * 资源请求评论返回DTO
 * 
 * @author auroral
 */
@Data
@Schema(description = "资源请求评论信息")
public class ResourceRequestReplyDTO {
    @Schema(description = "评论ID")
    private Long id;
    
    @Schema(description = "资源请求ID")
    private Long requestId;
    
    @Schema(description = "评论用户ID")
    private Long userId;
    
    @Schema(description = "评论用户名")
    private String username;
    
    @Schema(description = "评论用户头像")
    private String userAvatar;
    
    @Schema(description = "评论内容")
    private String content;
    
    @Schema(description = "父评论ID")
    private Long parentId;
    
    @Schema(description = "回复目标用户名")
    private String replyToUsername;
    
    @Schema(description = "关联资源ID")
    private Long resourceId;
    
    @Schema(description = "关联资源标题")
    private String resourceTitle;
    
    @Schema(description = "创建时间")
    private Date createTime;
    
    @Schema(description = "更新时间")
    private Date updateTime;
    
    @Schema(description = "是否删除")
    private Integer isDeleted;
    
    @Schema(description = "备注说明")
    private String remark;
    
    @Schema(description = "子评论列表")
    private List<ResourceRequestReplyDTO> children;
}
