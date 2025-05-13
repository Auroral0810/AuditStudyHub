package com.auditStudyHub.mapper;

import com.auditStudyHub.entity.TResourceComment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;

/**
 * 资源评论表(TResourceComment)表数据库访问层
 *
 * @author Auroral
 * @since 2025-05-06 11:28:26
 */
public interface TResourceCommentMapper extends BaseMapper<TResourceComment> {
    
    /**
     * 查询所有评论（包括已删除的评论）
     * 
     * @return 评论列表
     */
    @Select("SELECT * FROM t_resource_comment ORDER BY create_time DESC")
    List<TResourceComment> selectAllIncludeDeleted();
    /**
     * 根据搜索条件查询评论（包括已删除的评论）
     * 
     * @param content 评论内容
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param isDeleted 是否删除（可选）
     * @param parentId 父评论ID（可选）
     * @return 评论列表
     */
    @Select("<script>" +
            "SELECT * FROM t_resource_comment " +
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
    List<TResourceComment> searchAllIncludeDeleted(
            @Param("content") String content,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("isDeleted") Integer isDeleted,
            @Param("parentId") Long parentId);
    
    /**
     * 根据ID查询评论详情（包括已删除的评论）
     * 
     * @param id 评论ID
     * @return 评论详情
     */
    @Select("SELECT * FROM t_resource_comment WHERE id = #{id}")
    TResourceComment selectByIdWithDeleted(@Param("id") Long id);
    
    /**
     * 查询评论的子评论（包括已删除的评论）
     * 
     * @param parentId 父评论ID
     * @return 子评论列表
     */
    @Select("SELECT * FROM t_resource_comment WHERE parent_id = #{parentId} ORDER BY create_time ASC")
    List<TResourceComment> selectChildComments(@Param("parentId") Long parentId);
    
    /**
     * 根据资源ID查询评论（包括已删除的评论）
     * 
     * @param resourceId 资源ID
     * @return 评论列表
     */
    @Select("SELECT * FROM t_resource_comment WHERE resource_id = #{resourceId} ORDER BY create_time DESC")
    List<TResourceComment> selectCommentsByResourceId(@Param("resourceId") Long resourceId);

    /**
     * 更新评论状态（不考虑当前状态）
     * 
     * @param id 评论ID
     * @param isDeleted 是否删除，0-未删除，1-已删除
     * @param updateTime 更新时间
     * @return 受影响的行数
     */
    @Update("UPDATE t_resource_comment SET is_deleted = #{isDeleted}, update_time = #{updateTime} WHERE id = #{id}")
    int updateCommentStatus(@Param("id") Long id, @Param("isDeleted") Integer isDeleted, @Param("updateTime") Date updateTime);
}

