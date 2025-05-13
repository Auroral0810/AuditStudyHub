package com.auditStudyHub.mapper;

import com.auditStudyHub.entity.TResourceRequestReply;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;

/**
 * 资源求助回复表(TResourceRequestReply)表数据库访问层
 *
 * @author Auroral
 * @since 2025-05-06 11:28:34
 */
public interface TResourceRequestReplyMapper extends BaseMapper<TResourceRequestReply> {

    @Select("SELECT r.id, r.request_id, r.user_id, " +
            "CASE WHEN r.is_deleted = 1 THEN '该回复已删除' ELSE r.content END AS content, " +
            "r.parent_id, r.resource_id, r.create_time, r.update_time, r.is_deleted " +
            "FROM t_resource_request_reply r " +
            "WHERE r.request_id = #{requestId} " +
            "AND (r.is_deleted = 0 OR EXISTS (SELECT 1 FROM t_resource_request_reply child WHERE child.parent_id = r.id)) " +
            "ORDER BY r.create_time ASC")
    List<TResourceRequestReply> getAllRepliesWithDeleted(@Param("requestId") Long requestId);
    /**
     * 查询所有请求回复（包括已删除的）
     * 
     * @return 所有请求回复
     */
    @Select("SELECT * FROM t_resource_request_reply ORDER BY create_time DESC")
    List<TResourceRequestReply> selectAllIncludeDeleted();

    /**
     * 根据ID查询请求回复（包括已删除的）
     * 
     * @param id 请求回复ID
     * @return 请求回复
     */
    @Select("SELECT * FROM t_resource_request_reply WHERE id = #{id}")
    TResourceRequestReply selectByIdWithDeleted(@Param("id") Long id);
    
    /**
     * 根据请求ID查询所有回复（包括已删除的）
     * 
     * @param requestId 请求ID
     * @return 回复列表
     */
    @Select("SELECT * FROM t_resource_request_reply WHERE request_id = #{requestId} ORDER BY create_time ASC")
    List<TResourceRequestReply> selectByRequestIdWithDeleted(@Param("requestId") Long requestId);
    
    /**
     * 查询子回复（包括已删除的）
     * 
     * @param parentId 父回复ID
     * @return 子回复列表
     */
    @Select("SELECT * FROM t_resource_request_reply WHERE parent_id = #{parentId} ORDER BY create_time ASC")
    List<TResourceRequestReply> selectChildRepliesWithDeleted(@Param("parentId") Long parentId);
    
    /**
     * 根据搜索条件查询请求回复（包括已删除的）
     */
    @Select("<script>" +
            "SELECT * FROM t_resource_request_reply " +
            "WHERE 1=1 " +
            "<if test='content != null and content != \"\"'>" +
            "  AND content LIKE CONCAT('%', #{content}, '%') " +
            "</if>" +
            "<if test='startDate != null'>" +
            "  AND create_time &gt;= #{startDate} " +
            "</if>" +
            "<if test='endDate != null'>" +
            "  AND create_time &lt;= #{endDate} " +
            "</if>" +
            "<if test='isDeleted != null'>" +
            "  AND is_deleted = #{isDeleted} " +
            "</if>" +
            "<if test='parentId != null'>" +
            "  AND parent_id = #{parentId} " +
            "</if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    List<TResourceRequestReply> searchAllIncludeDeleted(
            @Param("content") String content,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("isDeleted") Integer isDeleted,
            @Param("parentId") Long parentId);
    
    /**
     * 更新请求回复内容
     * 
     * @param id 请求回复ID
     * @param content 新的内容
     * @param updateTime 更新时间
     * @return 受影响的行数
     */
    @Update("UPDATE t_resource_request_reply SET content = #{content}, update_time = #{updateTime} WHERE id = #{id}")
    int updateReplyContent(@Param("id") Long id, @Param("content") String content, @Param("updateTime") Date updateTime);
    
    /**
     * 更新请求回复状态（逻辑删除/恢复）
     * 
     * @param id 请求回复ID
     * @param isDeleted 是否删除：0-未删除，1-已删除
     * @param updateTime 更新时间
     * @return 受影响的行数
     */
    @Update("UPDATE t_resource_request_reply SET is_deleted = #{isDeleted}, update_time = #{updateTime} WHERE id = #{id}")
    int updateReplyStatus(@Param("id") Long id, @Param("isDeleted") Integer isDeleted, @Param("updateTime") Date updateTime);
}

