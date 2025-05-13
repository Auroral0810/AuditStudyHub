package com.auditStudyHub.mapper;

import com.auditStudyHub.entity.TResourceRequest;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;

/**
 * 资源求助表(TResourceRequest)表数据库访问层
 *
 * @author Auroral
 * @since 2025-05-06 11:28:34
 */
public interface TResourceRequestMapper extends BaseMapper<TResourceRequest> {
    
    /**
     * 查询所有资源请求（包括已删除的）
     * 
     * @return 资源请求列表
     */
    @Select("SELECT * FROM t_resource_request ORDER BY create_time DESC")
    List<TResourceRequest> selectAllIncludeDeleted();
    
    /**
     * 根据搜索条件查询资源请求（包括已删除的）
     * 
     * @param title 标题
     * @param content 内容
     * @param collegeId 学院ID
     * @param majorId 专业ID
     * @param categoryId 分类ID
     * @param status 状态
     * @param isDeleted 是否删除
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 资源请求列表
     */
    @Select("<script>" +
            "SELECT * FROM t_resource_request " +
            "WHERE 1=1 " +
            "<if test='title != null and title != \"\"'>" +
            "  AND title LIKE CONCAT('%', #{title}, '%') " +
            "</if>" +
            "<if test='content != null and content != \"\"'>" +
            "  AND content LIKE CONCAT('%', #{content}, '%') " +
            "</if>" +
            "<if test='collegeId != null'>" +
            "  AND college_id = #{collegeId} " +
            "</if>" +
            "<if test='majorId != null'>" +
            "  AND major_id = #{majorId} " +
            "</if>" +
            "<if test='categoryId != null'>" +
            "  AND category_id = #{categoryId} " +
            "</if>" +
            "<if test='status != null'>" +
            "  AND status = #{status} " +
            "</if>" +
            "<if test='isDeleted != null'>" +
            "  AND is_deleted = #{isDeleted} " +
            "</if>" +
            "<if test='startDate != null'>" +
            "  AND create_time &gt;= #{startDate} " +
            "</if>" +
            "<if test='endDate != null'>" +
            "  AND create_time &lt;= #{endDate} " +
            "</if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    List<TResourceRequest> searchAllIncludeDeleted(
            @Param("title") String title,
            @Param("content") String content,
            @Param("collegeId") Long collegeId,
            @Param("majorId") Long majorId,
            @Param("categoryId") Long categoryId,
            @Param("status") Integer status,
            @Param("isDeleted") Integer isDeleted,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);
    
    /**
     * 根据ID查询资源请求（包括已删除的）
     * 
     * @param id 请求ID
     * @return 资源请求
     */
    @Select("SELECT * FROM t_resource_request WHERE id = #{id}")
    TResourceRequest selectByIdWithDeleted(@Param("id") Long id);
    
    /**
     * 更新资源请求状态
     * 
     * @param id 请求ID
     * @param status 状态
     * @param updateTime 更新时间
     * @return 受影响的行数
     */
    @Update("UPDATE t_resource_request SET status = #{status}, update_time = #{updateTime} WHERE id = #{id}")
    int updateRequestStatus(@Param("id") Long id, @Param("status") Integer status, @Param("updateTime") Date updateTime);
    
    /**
     * 更新资源请求删除状态
     * 
     * @param id 请求ID
     * @param isDeleted 是否删除
     * @param updateTime 更新时间
     * @return 受影响的行数
     */
    @Update("UPDATE t_resource_request SET is_deleted = #{isDeleted}, update_time = #{updateTime} WHERE id = #{id}")
    int updateDeleteStatus(@Param("id") Long id, @Param("isDeleted") Integer isDeleted, @Param("updateTime") Date updateTime);
}

