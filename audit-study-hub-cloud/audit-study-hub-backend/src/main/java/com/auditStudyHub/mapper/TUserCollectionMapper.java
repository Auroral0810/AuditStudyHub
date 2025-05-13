package com.auditStudyHub.mapper;

import com.auditStudyHub.entity.TUserCollection;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Delete;

/**
 * 用户收藏表(TUserCollection)表数据库访问层
 *
 * @author Auroral
 * @since 2025-05-06 11:28:51
 */
public interface TUserCollectionMapper extends BaseMapper<TUserCollection> {
    
    /**
     * 查询用户是否已收藏指定资源
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 收藏记录ID，如果不存在则返回null
     */
    @Select("SELECT id FROM t_user_collection WHERE user_id = #{userId} AND resource_id = #{resourceId} AND is_deleted = 0")
    Long checkCollectionExists(@Param("userId") Long userId, @Param("resourceId") Long resourceId);
    
    /**
     * 查询已逻辑删除的收藏记录
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 收藏记录ID，如果不存在则返回null
     */
    @Select("SELECT id FROM t_user_collection WHERE user_id = #{userId} AND resource_id = #{resourceId} AND is_deleted = 1")
    Long checkDeletedCollectionExists(@Param("userId") Long userId, @Param("resourceId") Long resourceId);
    
    /**
     * 恢复已逻辑删除的收藏记录
     * @param id 收藏记录ID
     * @return 影响的行数
     */
    @Update("UPDATE t_user_collection SET is_deleted = 0 WHERE id = #{id}")
    int restoreCollection(@Param("id") Long id);
    
    /**
     * 软删除收藏记录
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 影响的行数
     */
    @Update("UPDATE t_user_collection SET is_deleted = 1 WHERE user_id = #{userId} AND resource_id = #{resourceId} AND is_deleted = 0")
    int softDeleteCollection(@Param("userId") Long userId, @Param("resourceId") Long resourceId);
    
    /**
     * 安全插入收藏记录（如果已存在且未删除则不插入）
     * @param userId 用户ID
     * @param resourceId 资源ID
     * @return 影响的行数
     */
    @Insert("INSERT INTO t_user_collection (user_id, resource_id, create_time, is_deleted) " +
            "SELECT #{userId}, #{resourceId}, NOW(), 0 " +
            "FROM DUAL " +
            "WHERE NOT EXISTS (SELECT 1 FROM t_user_collection WHERE user_id = #{userId} AND resource_id = #{resourceId} AND is_deleted = 0)")
    int safeInsertCollection(@Param("userId") Long userId, @Param("resourceId") Long resourceId);
}

