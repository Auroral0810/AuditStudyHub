package com.auditStudyHub.mapper;

import com.auditStudyHub.entity.TCollege;
import com.auditStudyHub.entity.TMajor;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;


/**
 * 专业表(TMajor)表数据库访问层
 *
 * @author Auroral
 * @since 2025-05-06 11:28:15
 */
public interface TMajorMapper extends BaseMapper<TMajor> {

    /**
     * 查询指定ID的专业（忽略逻辑删除）
     */
    @Select("SELECT * FROM t_major WHERE id = #{id}")
    TMajor selectByIdAll(Long id);

    /**
     * 查询所有专业（忽略逻辑删除）
     */
    @Select("SELECT * FROM t_major")
    List<TMajor> selectAllWithDeleted();

    /**
     * 更新专业（忽略逻辑删除）
     */
    @Update("UPDATE t_major " +
            "SET name = #{name}, " +
            "college_id = #{collegeId}, " +
            "degree = #{degree}, " +
            "xl = #{xl}, " +
            "xz = #{xz}, " +
            "update_time = #{updateTime}, " +
            "is_deleted = #{isDeleted} " +
            "WHERE id = #{id}")
    boolean updateWithDeleted(TMajor major);
    
    /**
     * 根据学院ID查询专业列表（忽略逻辑删除）
     */
    @Select("SELECT * FROM t_major WHERE college_id = #{collegeId}")
    List<TMajor> selectByCollegeIdWithDeleted(Long collegeId);

}

