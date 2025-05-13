package com.auditStudyHub.mapper;

import com.auditStudyHub.entity.TUserLike;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Delete;

/**
 * 用户点赞表(TUserLike)表数据库访问层
 *
 * @author Auroral
 * @since 2025-05-06 11:29:04
 */
public interface TUserLikeMapper extends BaseMapper<TUserLike> {
    
    /**
     * 查询用户是否已点赞指定资源
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 点赞记录ID，如果不存在则返回null
     */
    @Select("SELECT id FROM t_user_like WHERE user_id = #{userId} AND resource_id = #{resourceId} AND is_deleted = 0")
    Long checkLikeExists(@Param("userId") Long userId, @Param("resourceId") Long resourceId);
    
    /**
     * 查询已逻辑删除的点赞记录
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 点赞记录ID，如果不存在则返回null
     */
    @Select("SELECT id FROM t_user_like WHERE user_id = #{userId} AND resource_id = #{resourceId} AND is_deleted = 1")
    Long checkDeletedLikeExists(@Param("userId") Long userId, @Param("resourceId") Long resourceId);
    
    /**
     * 恢复已逻辑删除的点赞记录
     * @param id 点赞记录ID
     * @return 影响的行数
     */
    @Update("UPDATE t_user_like SET is_deleted = 0 WHERE id = #{id}")
    int restoreLike(@Param("id") Long id);
    
    /**
     * 软删除点赞记录
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 影响的行数
     */
    @Update("UPDATE t_user_like SET is_deleted = 1 WHERE user_id = #{userId} AND resource_id = #{resourceId} AND is_deleted = 0")
    int softDeleteLike(@Param("userId") Long userId, @Param("resourceId") Long resourceId);
    
    /**
     * 安全插入点赞记录（如果已存在且未删除则不插入）
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 影响的行数
     */
    @Insert("INSERT INTO t_user_like (user_id, resource_id, create_time, is_deleted) " +
            "SELECT #{userId}, #{resourceId}, NOW(), 0 " +
            "FROM DUAL " +
            "WHERE NOT EXISTS (SELECT 1 FROM t_user_like WHERE user_id = #{userId} AND resource_id = #{resourceId} AND is_deleted = 0)")
    int safeInsertLike(@Param("userId") Long userId, @Param("resourceId") Long resourceId);
    
    /**
     * 获取资源点赞数量
     * @param resourceId 资源ID
     * @return 点赞数量
     */
    @Select("SELECT COUNT(*) FROM t_user_like WHERE resource_id = #{resourceId} AND is_deleted = 0")
    int countResourceLikes(@Param("resourceId") Long resourceId);
}

