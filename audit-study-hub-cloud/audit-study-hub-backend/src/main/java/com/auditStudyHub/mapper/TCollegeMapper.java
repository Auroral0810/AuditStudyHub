package com.auditStudyHub.mapper;

import com.auditStudyHub.entity.TCategory;
import com.auditStudyHub.entity.TCollege;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;


/**
 * 学院表(TCollege)表数据库访问层
 *
 * @author Auroral
 * @since 2025-05-06 11:27:56
 */
public interface TCollegeMapper extends BaseMapper<TCollege> {

    @Select("SELECT * FROM t_college WHERE id = #{id}")
    TCollege selectByIdAll(Long id);

    @Select("SELECT * FROM t_college")
    List<TCollege> selectAllWithDeleted();

    @Update("UPDATE t_college " +
            "SET name = #{name}, " +
            "cover_url = #{coverUrl}, " +
            "logo_url = #{logoUrl}, " +
            "update_time = #{updateTime}, " +
            "is_deleted = #{isDeleted} " +
            "WHERE id = #{id}")
    boolean updateWithDeleted(TCollege college);

}

