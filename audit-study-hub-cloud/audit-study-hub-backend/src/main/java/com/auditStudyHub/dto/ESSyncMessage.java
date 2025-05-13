package com.auditStudyHub.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * ES同步消息DTO
 * 用于资源计数字段的异步同步
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ESSyncMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 资源ID
     */
    private Long resourceId;
    
    /**
     * 同步类型
     */
    private SyncType syncType;
    
    /**
     * 值变化（可用于增量更新）
     */
    private Integer valueChange;
    
    /**
     * 同步类型枚举
     * 使用String类型作为枚举值的存储方式，便于序列化和反序列化
     */
    public enum SyncType {
        VIEW_COUNT,       // 浏览量
        DOWNLOAD_COUNT,   // 下载量
        LIKE_COUNT,       // 点赞量
        FULL_SYNC,        // 全字段同步
        ALL;              // 所有字段同步(包括未审核状态的资源)
        
        @JsonCreator
        public static SyncType fromString(String value) {
            try {
                return SyncType.valueOf(value);
            } catch (Exception e) {
                return null;
            }
        }
    }
} 