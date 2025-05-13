package com.auditStudyHub.mapper;

import com.auditStudyHub.entity.TUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户表(TUser)表数据库访问层
 *
 * @author Auroral
 * @since 2025-05-06 11:28:44
 */
@Mapper
public interface TUserMapper extends BaseMapper<TUser> {

}

