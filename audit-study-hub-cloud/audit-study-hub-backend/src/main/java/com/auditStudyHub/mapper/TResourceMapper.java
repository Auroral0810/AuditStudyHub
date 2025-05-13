package com.auditStudyHub.mapper;

import com.auditStudyHub.entity.TResource;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Insert;

import java.util.Date;
import java.util.List;

/**
 * 资源表(TResource)表数据库访问层
 *
 * @author Auroral
 * @since 2025-05-06 11:28:20
 */
public interface TResourceMapper extends BaseMapper<TResource> {

    /**
     * 自定义查询：联表查询资源及上传者信息
     * 
     * @param title 标题关键词
     * @param uploaderName 上传者用户名
     * @param isDeleted 是否删除，可为null
     * @return 资源列表
     */
    @Select({
        "<script>",
        "SELECT r.*, u.username as uploader_name",
        "FROM t_resource r",
        "LEFT JOIN t_user u ON r.uploader_id = u.id",
        "WHERE 1=1",
        "<if test='title != null and title != \"\"'>",
        "  AND r.title LIKE CONCAT('%', #{title}, '%')",
        "</if>",
        "<if test='uploaderName != null and uploaderName != \"\"'>",
        "  AND u.username LIKE CONCAT('%', #{uploaderName}, '%')",
        "</if>",
        "<if test='isDeleted != null'>",
        "  AND r.is_deleted = #{isDeleted}",
        "</if>",
        "ORDER BY r.create_time DESC",
        "</script>"
    })
    List<TResource> selectWithUploaderName(
        @Param("title") String title, 
        @Param("uploaderName") String uploaderName, 
        @Param("isDeleted") Integer isDeleted
    );

    /**
     * 查询所有资源（包括已删除的）
     * 
     * @return 所有资源列表
     */
    @Select("SELECT * FROM t_resource ORDER BY create_time DESC")
    List<TResource> selectAllWithDeleted();
    
    /**
     * 根据查询条件获取资源（包括已删除的）
     */
    @Select({
        "<script>",
        "SELECT r.* FROM t_resource r",
        "LEFT JOIN t_user u ON r.uploader_id = u.id",
        "WHERE 1=1",
        "<if test='title != null and title != \"\"'>",
        "  AND r.title LIKE CONCAT('%', #{title}, '%')",
        "</if>",
        "<if test='uploaderName != null and uploaderName != \"\"'>",
        "  AND u.username LIKE CONCAT('%', #{uploaderName}, '%')",
        "</if>",
        "<if test='isDeleted != null'>",
        "  AND r.is_deleted = #{isDeleted}",
        "</if>",
        "ORDER BY r.create_time DESC",
        "</script>"
    })
    List<TResource> selectAllWithCondition(
        @Param("title") String title, 
        @Param("uploaderName") String uploaderName, 
        @Param("isDeleted") Integer isDeleted
    );

    /**
     * 更新资源状态（逻辑删除或恢复）
     */
    @Update("UPDATE t_resource SET is_deleted = #{isDeleted}, update_time = #{updateTime} WHERE id = #{id}")
    int updateResourceStatus(
        @Param("id") Long id, 
        @Param("isDeleted") Integer isDeleted, 
        @Param("updateTime") Date updateTime
    );
    @Select("SELECT * FROM t_resource WHERE id = #{id}")
    TResource selectByIdWithDeleted(Long id);
}

