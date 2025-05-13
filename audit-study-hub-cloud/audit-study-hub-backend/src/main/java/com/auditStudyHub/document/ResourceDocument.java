package com.auditStudyHub.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.util.Date;

/**
 * 资源文档模型（对应Elasticsearch中的文档）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "resources")
@Setting(settingPath = "elastic/es-settings.json")
public class ResourceDocument {
    
    @Id
    private Long id;
    
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;
    
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String description;
    
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String tags;
    
    @Field(type = FieldType.Keyword)
    private String fileType;
    
    @Field(type = FieldType.Long)
    private Long fileSize;

    @Field(type = FieldType.Keyword)
    private String fileUrl;
    
    @Field(type = FieldType.Integer)
    private Integer downloadCount;
    
    @Field(type = FieldType.Integer)
    private Integer viewCount;
    
    @Field(type = FieldType.Integer)
    private Integer likeCount;
    
    @Field(type = FieldType.Long)
    private Long uploaderId;
    
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String uploaderName;
    
    @Field(type = FieldType.Long)
    private Long collegeId;
    
    @Field(type = FieldType.Keyword)
    private String collegeName;
    
    @Field(type = FieldType.Long)
    private Long majorId;
    
    @Field(type = FieldType.Keyword)
    private String majorName;
    
    @Field(type = FieldType.Long)
    private Long categoryId;
    
    @Field(type = FieldType.Keyword)
    private String categoryName;
    
    @Field(type = FieldType.Integer)
    private Integer status;
    
    @Field(type = FieldType.Integer)
    private Integer isDeleted;
    
    @Field(type = FieldType.Date)
    private Date createTime;
    
    @Field(type = FieldType.Date)
    private Date updateTime;
}
