package com.auditStudyHub.mapper;

import com.auditStudyHub.entity.TCourse;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 课程表(TCourse)表数据库访问层
 *
 * @author Auroral
 * @since 2025-05-06 11:28:07
 */
public interface TCourseMapper extends BaseMapper<TCourse> {

    /**
     * 查询所有课程（包括已删除的）
     */
    @Select("SELECT * FROM t_course")
    List<TCourse> selectAllWithDeleted();

    /**
     * 根据ID查询课程（包括已删除的）
     */
    @Select("SELECT * FROM t_course WHERE id = #{id}")
    TCourse selectByIdWithDeleted(Long id);

    /**
     * 更新课程（包括已删除的）
     */
    @Update("UPDATE t_course " +
            "SET name = #{name}, " +
            "description = #{description}, " +
            "credit = #{credit}, " +
            "update_time = #{updateTime}, " +
            "is_deleted = #{isDeleted} " +
            "WHERE id = #{id}")
    boolean updateWithDeleted(TCourse course);
    
    /**
     * 根据关键词搜索课程（包括已删除的）
     */
    @Select("SELECT * FROM t_course WHERE name LIKE CONCAT('%', #{keyword}, '%') " +
           "OR description LIKE CONCAT('%', #{keyword}, '%')")
    List<TCourse> searchWithDeleted(String keyword);
}

