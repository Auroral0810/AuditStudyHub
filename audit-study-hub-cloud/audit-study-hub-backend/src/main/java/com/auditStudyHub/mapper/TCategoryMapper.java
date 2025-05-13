package com.auditStudyHub.mapper;

import com.auditStudyHub.entity.TCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;


/**
 * 资源分类表(TCategory)表数据库访问层
 *
 * @author Auroral
 * @since 2025-05-06 11:25:54
 */
public interface TCategoryMapper extends BaseMapper<TCategory> {

    @Select("SELECT * FROM t_category WHERE id = #{id}")
    TCategory selectByIdAll(Long id);

    @Select("SELECT id, name, icon, sort, create_time, update_time, is_deleted FROM t_category")
    List<TCategory> selectAllWithDeleted();

    @Update("UPDATE t_category " +
            "SET name = #{name}, " +
            "icon = #{icon}, " +
            "sort = #{sort}, " +
            "update_time = #{updateTime}, " +
            "is_deleted = #{isDeleted} " +
            "WHERE id = #{id}")
    boolean updateWithDeleted(TCategory category);
    
    /**
     * 根据名称或图标搜索分类（包括已删除的）
     */
    @Select("SELECT * FROM t_category WHERE name LIKE CONCAT('%', #{keyword}, '%') " +
           "OR icon LIKE CONCAT('%', #{keyword}, '%')")
    List<TCategory> searchWithDeleted(String keyword);
}

