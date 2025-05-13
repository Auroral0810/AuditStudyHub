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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "resource_requests")
@Setting(settingPath = "elastic/es-settings.json")
public class ResourceRequestDocument {
    @Id
    private Long id;
    
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;
    
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;
    
    @Field(type = FieldType.Long)
    private Long userId;
    
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String username;
    
    @Field(type = FieldType.Long)
    private Long collegeId;
    
    @Field(type = FieldType.Keyword)
    private String collegeName;
    
    @Field(type = FieldType.Long)
    private Long majorId;
    
    @Field(type = FieldType.Keyword)
    private String majorName;
    
    @Field(type = FieldType.Long)
    private Long courseId;
    
    @Field(type = FieldType.Keyword)
    private String courseName;
    
    @Field(type = FieldType.Long)
    private Long categoryId;
    
    @Field(type = FieldType.Keyword)
    private String categoryName;
    
    @Field(type = FieldType.Integer)
    private Integer status;
    
    @Field(type = FieldType.Integer)
    private Integer replyCount;
    
    @Field(type = FieldType.Integer)
    private Integer viewCount;
    
    @Field(type = FieldType.Date)
    private Date createTime;
    
    @Field(type = FieldType.Date)
    private Date updateTime;
}
