/*
 Navicat Premium Data Transfer

 Source Server         : yyf
 Source Server Type    : MySQL
 Source Server Version : 80036 (8.0.36)
 Source Host           : localhost:3306
 Source Schema         : audit_study_hub

 Target Server Type    : MySQL
 Target Server Version : 80036 (8.0.36)
 File Encoding         : 65001

 Date: 12/05/2025 14:49:49
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for t_api_access_log
-- ----------------------------
DROP TABLE IF EXISTS `t_api_access_log`;
CREATE TABLE `t_api_access_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `username` varchar(64) NOT NULL COMMENT '用户名',
  `api_id` bigint DEFAULT NULL COMMENT '接口ID',
  `api_url` varchar(255) NOT NULL COMMENT '接口URL',
  `http_method` varchar(10) NOT NULL COMMENT '请求方法',
  `ip_address` varchar(64) NOT NULL COMMENT 'IP地址',
  `request_params` text COMMENT '请求参数',
  `request_body` longtext COMMENT '请求体',
  `response_code` int DEFAULT NULL COMMENT '响应状态码',
  `response_body` longtext COMMENT '响应内容',
  `execute_time` bigint DEFAULT NULL COMMENT '执行时间(毫秒)',
  `status` tinyint DEFAULT NULL COMMENT '状态(0-失败,1-成功)',
  `error_message` text COMMENT '错误信息',
  `user_agent` varchar(255) DEFAULT NULL COMMENT '用户代理',
  `referer` varchar(255) DEFAULT NULL COMMENT '来源页面',
  `access_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_api_id` (`api_id`),
  KEY `idx_api_url` (`api_url`(191)),
  KEY `idx_access_time` (`access_time`),
  KEY `idx_status` (`status`),
  KEY `idx_execute_time` (`execute_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='接口访问日志表';

-- ----------------------------
-- Table structure for t_api_permission
-- ----------------------------
DROP TABLE IF EXISTS `t_api_permission`;
CREATE TABLE `t_api_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `api_id` bigint NOT NULL COMMENT '接口ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_api_permission` (`api_id`,`permission_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='接口与权限关联表';

-- ----------------------------
-- Table structure for t_api_resource
-- ----------------------------
DROP TABLE IF EXISTS `t_api_resource`;
CREATE TABLE `t_api_resource` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '接口ID',
  `api_code` varchar(64) NOT NULL COMMENT '接口编码',
  `api_name` varchar(100) NOT NULL COMMENT '接口名称',
  `api_url` varchar(255) NOT NULL COMMENT '接口URL',
  `http_method` varchar(10) NOT NULL COMMENT '请求方法(GET,POST,PUT,DELETE)',
  `module` varchar(50) NOT NULL COMMENT '所属模块',
  `description` varchar(255) DEFAULT NULL COMMENT '接口描述',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0-禁用,1-启用)',
  `is_auth` tinyint NOT NULL DEFAULT '1' COMMENT '是否需要认证(0-不需要,1-需要)',
  `service_name` varchar(100) DEFAULT NULL COMMENT '服务名称',
  `doc_url` varchar(255) DEFAULT NULL COMMENT '接口文档URL',
  `request_example` text COMMENT '请求示例',
  `response_example` text COMMENT '响应示例',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建者ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新者ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_api_code` (`api_code`),
  KEY `idx_api_url` (`api_url`(191)),
  KEY `idx_module` (`module`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='接口资源表';

-- ----------------------------
-- Table structure for t_category
-- ----------------------------
DROP TABLE IF EXISTS `t_category`;
CREATE TABLE `t_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `icon` varchar(100) DEFAULT NULL COMMENT '分类图标',
  `sort` int DEFAULT '0' COMMENT '排序',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除，0-未删除，1-已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资源分类表';

-- ----------------------------
-- Table structure for t_college
-- ----------------------------
DROP TABLE IF EXISTS `t_college`;
CREATE TABLE `t_college` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '学院ID',
  `name` varchar(100) NOT NULL COMMENT '学院名称',
  `cover_url` varchar(255) DEFAULT NULL COMMENT '学院封面图片URL',
  `logo_url` varchar(255) DEFAULT NULL COMMENT '学院logo图片URL',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除，0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=1747 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学院表';

-- ----------------------------
-- Table structure for t_course
-- ----------------------------
DROP TABLE IF EXISTS `t_course`;
CREATE TABLE `t_course` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  `name` varchar(100) NOT NULL COMMENT '课程名称',
  `description` varchar(500) DEFAULT NULL COMMENT '课程描述',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除，0-未删除，1-已删除',
  `credit` decimal(5,2) DEFAULT NULL COMMENT '课程学分',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1329 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='课程表';

-- ----------------------------
-- Table structure for t_hot_search
-- ----------------------------
DROP TABLE IF EXISTS `t_hot_search`;
CREATE TABLE `t_hot_search` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '搜索记录ID',
  `keyword` varchar(100) NOT NULL COMMENT '搜索关键词',
  `search_count` int NOT NULL DEFAULT '1' COMMENT '搜索次数',
  `last_search_time` datetime NOT NULL COMMENT '最近搜索时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除，0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_keyword` (`keyword`),
  KEY `idx_search_count` (`search_count`),
  KEY `idx_last_search_time` (`last_search_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1920111957084590098 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='搜索记录表';

-- ----------------------------
-- Table structure for t_major
-- ----------------------------
DROP TABLE IF EXISTS `t_major`;
CREATE TABLE `t_major` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '专业ID',
  `name` varchar(100) NOT NULL COMMENT '专业名称',
  `college_id` bigint NOT NULL COMMENT '所属学院ID',
  `degree` varchar(50) DEFAULT NULL COMMENT '学位类型',
  `xl` tinyint DEFAULT '0' COMMENT '学历等级',
  `xz` varchar(20) DEFAULT NULL COMMENT '学制',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除，0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_college_id` (`college_id`),
  CONSTRAINT `fk_major_college` FOREIGN KEY (`college_id`) REFERENCES `t_college` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19210 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='专业表';

-- ----------------------------
-- Table structure for t_menu
-- ----------------------------
DROP TABLE IF EXISTS `t_menu`;
CREATE TABLE `t_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父菜单ID',
  `menu_name` varchar(50) NOT NULL COMMENT '菜单名称',
  `menu_type` char(1) NOT NULL COMMENT '菜单类型(M-目录,C-菜单,F-按钮)',
  `order_num` int DEFAULT '0' COMMENT '显示顺序',
  `path` varchar(200) DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) DEFAULT NULL COMMENT '组件路径',
  `query` varchar(255) DEFAULT NULL COMMENT '路由参数',
  `is_frame` tinyint DEFAULT '1' COMMENT '是否为外链(0-是,1-否)',
  `is_cache` tinyint DEFAULT '0' COMMENT '是否缓存(0-不缓存,1-缓存)',
  `is_visible` tinyint DEFAULT '0' COMMENT '是否显示(0-显示,1-隐藏)',
  `status` tinyint DEFAULT '0' COMMENT '状态(0-正常,1-停用)',
  `perms` varchar(100) DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) DEFAULT '#' COMMENT '菜单图标',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建者ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新者ID',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_order_num` (`order_num`),
  KEY `idx_menu_type` (`menu_type`),
  KEY `idx_status` (`status`),
  KEY `idx_visible` (`is_visible`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单权限表';

-- ----------------------------
-- Table structure for t_menu_permission
-- ----------------------------
DROP TABLE IF EXISTS `t_menu_permission`;
CREATE TABLE `t_menu_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_menu_permission` (`menu_id`,`permission_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单与权限关联表';

-- ----------------------------
-- Table structure for t_permission
-- ----------------------------
DROP TABLE IF EXISTS `t_permission`;
CREATE TABLE `t_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `parent_id` bigint DEFAULT NULL COMMENT '父权限ID',
  `name` varchar(50) NOT NULL COMMENT '权限名称',
  `code` varchar(50) NOT NULL COMMENT '权限编码',
  `type` tinyint NOT NULL COMMENT '权限类型：1-菜单，2-按钮，3-API',
  `path` varchar(200) DEFAULT NULL COMMENT '前端路由路径',
  `component` varchar(255) DEFAULT NULL COMMENT '前端组件路径',
  `icon` varchar(100) DEFAULT NULL COMMENT '菜单图标',
  `permission_value` varchar(255) DEFAULT NULL COMMENT '权限值（操作权限）',
  `redirect` varchar(255) DEFAULT NULL COMMENT '重定向地址',
  `sort` int DEFAULT '0' COMMENT '排序',
  `hidden` tinyint NOT NULL DEFAULT '0' COMMENT '是否隐藏：0-显示，1-隐藏',
  `always_show` tinyint DEFAULT '0' COMMENT '是否总是显示：0-否，1-是',
  `keep_alive` tinyint DEFAULT '0' COMMENT '缓存该页面：0-否，1-是',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除，0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`code`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=20105 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='权限表';

-- ----------------------------
-- Table structure for t_popular_search
-- ----------------------------
DROP TABLE IF EXISTS `t_popular_search`;
CREATE TABLE `t_popular_search` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '热门搜索ID',
  `keyword` varchar(100) NOT NULL COMMENT '热门关键词',
  `rank` int NOT NULL DEFAULT '0' COMMENT '排序权重',
  `count_weight` int NOT NULL DEFAULT '0' COMMENT '统计权重',
  `is_manual` tinyint NOT NULL DEFAULT '0' COMMENT '是否手动添加，0-自动统计，1-手动添加',
  `start_time` datetime DEFAULT NULL COMMENT '生效开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '生效结束时间',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态，0-下线，1-上线',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除，0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_keyword` (`keyword`),
  KEY `idx_rank` (`rank`),
  KEY `idx_status` (`status`),
  KEY `idx_start_end_time` (`start_time`,`end_time`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='热门搜索词表';

-- ----------------------------
-- Table structure for t_resource
-- ----------------------------
DROP TABLE IF EXISTS `t_resource`;
CREATE TABLE `t_resource` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '资源ID',
  `title` varchar(100) NOT NULL COMMENT '资源标题',
  `description` text COMMENT '资源描述',
  `file_url` varchar(255) NOT NULL COMMENT '文件URL',
  `file_type` varchar(20) NOT NULL COMMENT '文件类型',
  `file_size` bigint NOT NULL COMMENT '文件大小（字节）',
  `download_count` int NOT NULL DEFAULT '0' COMMENT '下载次数',
  `view_count` int NOT NULL DEFAULT '0' COMMENT '浏览次数',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞次数',
  `uploader_id` bigint NOT NULL COMMENT '上传者ID',
  `college_id` bigint DEFAULT NULL COMMENT '学院ID',
  `college_name` varchar(100) DEFAULT NULL COMMENT '学院名称',
  `major_id` bigint DEFAULT NULL COMMENT '专业ID',
  `major_name` varchar(100) DEFAULT NULL COMMENT '专业名称',
  `course_id` bigint DEFAULT NULL COMMENT '课程ID',
  `course_name` varchar(100) DEFAULT NULL COMMENT '课程名称',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `category_name` varchar(50) DEFAULT NULL COMMENT '分类名称',
  `tags` varchar(255) DEFAULT NULL COMMENT '标签，多个标签用逗号分隔',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态，0-待审核，1-已通过，2-已拒绝',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除，0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_uploader_id` (`uploader_id`),
  KEY `idx_college_id` (`college_id`),
  KEY `idx_major_id` (`major_id`),
  KEY `idx_course_id` (`course_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_status` (`status`),
  FULLTEXT KEY `ft_title_desc_tags` (`title`,`description`,`tags`),
  CONSTRAINT `fk_resource_category` FOREIGN KEY (`category_id`) REFERENCES `t_category` (`id`),
  CONSTRAINT `fk_resource_college` FOREIGN KEY (`college_id`) REFERENCES `t_college` (`id`),
  CONSTRAINT `fk_resource_course` FOREIGN KEY (`course_id`) REFERENCES `t_course` (`id`),
  CONSTRAINT `fk_resource_major` FOREIGN KEY (`major_id`) REFERENCES `t_major` (`id`),
  CONSTRAINT `fk_resource_user` FOREIGN KEY (`uploader_id`) REFERENCES `t_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资源表';

-- ----------------------------
-- Table structure for t_resource_comment
-- ----------------------------
DROP TABLE IF EXISTS `t_resource_comment`;
CREATE TABLE `t_resource_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `resource_id` bigint NOT NULL COMMENT '资源ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `content` text NOT NULL COMMENT '评论内容',
  `parent_id` bigint DEFAULT NULL COMMENT '父评论ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除，0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_resource_id` (`resource_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `t_resource_comment` (`id`),
  CONSTRAINT `fk_comment_resource` FOREIGN KEY (`resource_id`) REFERENCES `t_resource` (`id`),
  CONSTRAINT `fk_comment_user` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资源评论表';

-- ----------------------------
-- Table structure for t_resource_request
-- ----------------------------
DROP TABLE IF EXISTS `t_resource_request`;
CREATE TABLE `t_resource_request` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '求助ID',
  `title` varchar(100) NOT NULL COMMENT '求助标题',
  `content` text NOT NULL COMMENT '求助内容',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `college_id` bigint DEFAULT NULL COMMENT '学院ID',
  `college_name` varchar(100) DEFAULT NULL COMMENT '学院名称',
  `major_id` bigint DEFAULT NULL COMMENT '专业ID',
  `major_name` varchar(100) DEFAULT NULL COMMENT '专业名称',
  `course_id` bigint DEFAULT NULL COMMENT '课程ID',
  `course_name` varchar(100) DEFAULT NULL COMMENT '课程名称',
  `category_id` bigint DEFAULT NULL COMMENT '分类ID',
  `category_name` varchar(50) DEFAULT NULL COMMENT '分类名称',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态，0-未解决，1-已解决',
  `reply_count` int NOT NULL DEFAULT '0' COMMENT '回复数量',
  `view_count` int NOT NULL DEFAULT '0' COMMENT '浏览数量',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除，0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_college_id` (`college_id`),
  KEY `idx_major_id` (`major_id`),
  KEY `idx_course_id` (`course_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_status` (`status`),
  KEY `idx_category_id` (`category_id`),
  FULLTEXT KEY `ft_title_content` (`title`,`content`),
  CONSTRAINT `fk_request_category` FOREIGN KEY (`category_id`) REFERENCES `t_category` (`id`),
  CONSTRAINT `fk_request_college` FOREIGN KEY (`college_id`) REFERENCES `t_college` (`id`),
  CONSTRAINT `fk_request_course` FOREIGN KEY (`course_id`) REFERENCES `t_course` (`id`),
  CONSTRAINT `fk_request_major` FOREIGN KEY (`major_id`) REFERENCES `t_major` (`id`),
  CONSTRAINT `fk_request_user` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资源求助表';

-- ----------------------------
-- Table structure for t_resource_request_reply
-- ----------------------------
DROP TABLE IF EXISTS `t_resource_request_reply`;
CREATE TABLE `t_resource_request_reply` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '回复ID',
  `request_id` bigint NOT NULL COMMENT '求助ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `content` text NOT NULL COMMENT '回复内容',
  `resource_id` bigint DEFAULT NULL COMMENT '关联资源ID',
  `parent_id` bigint DEFAULT NULL COMMENT '父回复ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除，0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_request_id` (`request_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_resource_id` (`resource_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_parent_id` (`parent_id`),
  CONSTRAINT `fk_reply_parent` FOREIGN KEY (`parent_id`) REFERENCES `t_resource_request_reply` (`id`),
  CONSTRAINT `fk_reply_request` FOREIGN KEY (`request_id`) REFERENCES `t_resource_request` (`id`),
  CONSTRAINT `fk_reply_resource` FOREIGN KEY (`resource_id`) REFERENCES `t_resource` (`id`),
  CONSTRAINT `fk_reply_user` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='资源求助回复表';

-- ----------------------------
-- Table structure for t_role
-- ----------------------------
DROP TABLE IF EXISTS `t_role`;
CREATE TABLE `t_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `name` varchar(50) NOT NULL COMMENT '角色名称',
  `code` varchar(50) NOT NULL COMMENT '角色编码',
  `description` varchar(255) DEFAULT NULL COMMENT '角色描述',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除，0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

-- ----------------------------
-- Table structure for t_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `t_role_permission`;
CREATE TABLE `t_role_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_permission` (`role_id`,`permission_id`),
  KEY `idx_permission_id` (`permission_id`),
  CONSTRAINT `fk_rp_permission` FOREIGN KEY (`permission_id`) REFERENCES `t_permission` (`id`),
  CONSTRAINT `fk_rp_role` FOREIGN KEY (`role_id`) REFERENCES `t_role` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色-权限关联表';

-- ----------------------------
-- Table structure for t_user
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `student_id` varchar(50) DEFAULT NULL COMMENT '学号',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `college_id` bigint DEFAULT NULL COMMENT '学院ID',
  `college_name` varchar(100) DEFAULT NULL COMMENT '学院名称',
  `major_id` bigint DEFAULT NULL COMMENT '专业ID',
  `major_name` varchar(100) DEFAULT NULL COMMENT '专业名称',
  `role` tinyint NOT NULL DEFAULT '0' COMMENT '角色，0-普通用户，1-管理员',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态，0-禁用，1-启用',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除，0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_student_id` (`student_id`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_college_id` (`college_id`),
  KEY `idx_major_id` (`major_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

-- ----------------------------
-- Table structure for t_user_collection
-- ----------------------------
DROP TABLE IF EXISTS `t_user_collection`;
CREATE TABLE `t_user_collection` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `resource_id` bigint NOT NULL COMMENT '资源ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除，0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_resource` (`user_id`,`resource_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_resource_id` (`resource_id`),
  CONSTRAINT `fk_collection_resource` FOREIGN KEY (`resource_id`) REFERENCES `t_resource` (`id`),
  CONSTRAINT `fk_collection_user` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户收藏表';

-- ----------------------------
-- Table structure for t_user_download
-- ----------------------------
DROP TABLE IF EXISTS `t_user_download`;
CREATE TABLE `t_user_download` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '下载ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `resource_id` bigint NOT NULL COMMENT '资源ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_resource_id` (`resource_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_download_resource` FOREIGN KEY (`resource_id`) REFERENCES `t_resource` (`id`),
  CONSTRAINT `fk_download_user` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户下载表';

-- ----------------------------
-- Table structure for t_user_like
-- ----------------------------
DROP TABLE IF EXISTS `t_user_like`;
CREATE TABLE `t_user_like` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `resource_id` bigint NOT NULL COMMENT '资源ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `is_deleted` tinyint NOT NULL DEFAULT '0' COMMENT '是否删除，0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_resource` (`user_id`,`resource_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_resource_id` (`resource_id`),
  CONSTRAINT `fk_like_resource` FOREIGN KEY (`resource_id`) REFERENCES `t_resource` (`id`),
  CONSTRAINT `fk_like_user` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户点赞表';

-- ----------------------------
-- Table structure for t_user_role
-- ----------------------------
DROP TABLE IF EXISTS `t_user_role`;
CREATE TABLE `t_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
  KEY `idx_role_id` (`role_id`),
  CONSTRAINT `fk_ur_role` FOREIGN KEY (`role_id`) REFERENCES `t_role` (`id`),
  CONSTRAINT `fk_ur_user` FOREIGN KEY (`user_id`) REFERENCES `t_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户-角色关联表';

-- ----------------------------
-- Procedure structure for record_search_keyword
-- ----------------------------
DROP PROCEDURE IF EXISTS `record_search_keyword`;
delimiter ;;
CREATE PROCEDURE `record_search_keyword`(IN p_keyword VARCHAR(100))
BEGIN
    DECLARE v_count INT;

    -- 检查关键词是否已存在
    SELECT COUNT(*) INTO v_count FROM t_hot_search WHERE keyword = p_keyword AND is_deleted = 0;

    IF v_count > 0 THEN
        -- 更新已有记录
        UPDATE t_hot_search
        SET search_count = search_count + 1,
            last_search_time = NOW(),
            update_time = NOW()
        WHERE keyword = p_keyword AND is_deleted = 0;
    ELSE
        -- 插入新记录
        INSERT INTO t_hot_search (
            keyword,
            search_count,
            last_search_time,
            create_time,
            update_time
        ) VALUES (
                     p_keyword,
                     1,
                     NOW(),
                     NOW(),
                     NOW()
                 );
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for update_popular_search
-- ----------------------------
DROP PROCEDURE IF EXISTS `update_popular_search`;
delimiter ;;
CREATE PROCEDURE `update_popular_search`()
BEGIN
    -- 清除过期的手动数据
    UPDATE t_popular_search
    SET status = 0
    WHERE is_manual = 1 AND end_time IS NOT NULL AND end_time < NOW();

    -- 删除旧的自动记录
    DELETE FROM t_popular_search WHERE is_manual = 0;

    -- 从搜索记录表插入新的热门词（简化版）
    INSERT INTO t_popular_search
    (keyword, count_weight, `rank`, is_manual, status, create_time, update_time)
    SELECT
        h.keyword,
        h.search_count,
        0,
        0,
        1,
        NOW(),
        NOW()
    FROM t_hot_search h
    WHERE h.is_deleted = 0
      AND h.last_search_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
    ORDER BY h.search_count DESC
    LIMIT 10;

    -- 更新排序权重（简化版）
    SET @r := 0;
    UPDATE t_popular_search p
    SET p.`rank` = (@r := @r + 1)
    WHERE p.status = 1
    ORDER BY p.is_manual DESC, p.count_weight DESC;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table t_category
-- ----------------------------
DROP TRIGGER IF EXISTS `update_category_name_in_resource`;
delimiter ;;
CREATE TRIGGER `update_category_name_in_resource` AFTER UPDATE ON `t_category` FOR EACH ROW BEGIN
    IF NEW.name <> OLD.name AND NEW.is_deleted = 0 THEN
        UPDATE t_resource
        SET category_name = NEW.name
        WHERE category_id = NEW.id AND is_deleted = 0;
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table t_category
-- ----------------------------
DROP TRIGGER IF EXISTS `update_category_name_in_resource_request`;
delimiter ;;
CREATE TRIGGER `update_category_name_in_resource_request` AFTER UPDATE ON `t_category` FOR EACH ROW BEGIN
    IF NEW.name <> OLD.name AND NEW.is_deleted = 0 THEN
        UPDATE t_resource_request
        SET category_name = NEW.name
        WHERE category_id = NEW.id AND is_deleted = 0;
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table t_college
-- ----------------------------
DROP TRIGGER IF EXISTS `update_college_name_in_resource`;
delimiter ;;
CREATE TRIGGER `update_college_name_in_resource` AFTER UPDATE ON `t_college` FOR EACH ROW BEGIN
    IF NEW.name <> OLD.name AND NEW.is_deleted = 0 THEN
        UPDATE t_resource
        SET college_name = NEW.name
        WHERE college_id = NEW.id AND is_deleted = 0;

        UPDATE t_resource_request
        SET college_name = NEW.name
        WHERE college_id = NEW.id AND is_deleted = 0;

        UPDATE t_user
        SET college_name = NEW.name
        WHERE college_id = NEW.id AND is_deleted = 0;
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table t_course
-- ----------------------------
DROP TRIGGER IF EXISTS `update_course_name_in_resource`;
delimiter ;;
CREATE TRIGGER `update_course_name_in_resource` AFTER UPDATE ON `t_course` FOR EACH ROW BEGIN
    IF NEW.name <> OLD.name AND NEW.is_deleted = 0 THEN
        UPDATE t_resource
        SET course_name = NEW.name
        WHERE course_id = NEW.id AND is_deleted = 0;

        UPDATE t_resource_request
        SET course_name = NEW.name
        WHERE course_id = NEW.id AND is_deleted = 0;
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table t_major
-- ----------------------------
DROP TRIGGER IF EXISTS `update_major_name_in_resource`;
delimiter ;;
CREATE TRIGGER `update_major_name_in_resource` AFTER UPDATE ON `t_major` FOR EACH ROW BEGIN
    IF NEW.name <> OLD.name AND NEW.is_deleted = 0 THEN
        UPDATE t_resource
        SET major_name = NEW.name
        WHERE major_id = NEW.id AND is_deleted = 0;

        UPDATE t_resource_request
        SET major_name = NEW.name
        WHERE major_id = NEW.id AND is_deleted = 0;

        UPDATE t_user
        SET major_name = NEW.name
        WHERE major_id = NEW.id AND is_deleted = 0;
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table t_resource
-- ----------------------------
DROP TRIGGER IF EXISTS `set_college_name_before_insert_resource`;
delimiter ;;
CREATE TRIGGER `set_college_name_before_insert_resource` BEFORE INSERT ON `t_resource` FOR EACH ROW BEGIN
    DECLARE v_college_name VARCHAR(100);
    DECLARE v_major_name VARCHAR(100);
    DECLARE v_course_name VARCHAR(100);
    DECLARE v_category_name VARCHAR(50);

    -- 设置学院名称
    IF NEW.college_id IS NOT NULL THEN
        SELECT name INTO v_college_name
        FROM t_college
        WHERE id = NEW.college_id AND is_deleted = 0
        LIMIT 1;

        SET NEW.college_name = v_college_name;
    END IF;

    -- 设置专业名称
    IF NEW.major_id IS NOT NULL THEN
        SELECT name INTO v_major_name
        FROM t_major
        WHERE id = NEW.major_id AND is_deleted = 0
        LIMIT 1;

        SET NEW.major_name = v_major_name;
    END IF;

    -- 设置课程名称
    IF NEW.course_id IS NOT NULL THEN
        SELECT name INTO v_course_name
        FROM t_course
        WHERE id = NEW.course_id AND is_deleted = 0
        LIMIT 1;

        SET NEW.course_name = v_course_name;
    END IF;

    -- 设置分类名称
    IF NEW.category_id IS NOT NULL THEN
        SELECT name INTO v_category_name
        FROM t_category
        WHERE id = NEW.category_id AND is_deleted = 0
        LIMIT 1;

        SET NEW.category_name = v_category_name;
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table t_resource
-- ----------------------------
DROP TRIGGER IF EXISTS `set_names_before_update_resource`;
delimiter ;;
CREATE TRIGGER `set_names_before_update_resource` BEFORE UPDATE ON `t_resource` FOR EACH ROW BEGIN
    DECLARE v_college_name VARCHAR(100);
    DECLARE v_major_name VARCHAR(100);
    DECLARE v_course_name VARCHAR(100);
    DECLARE v_category_name VARCHAR(50);

    -- 如果学院ID改变，更新学院名称
    IF NEW.college_id <> OLD.college_id AND NEW.college_id IS NOT NULL THEN
        SELECT name INTO v_college_name
        FROM t_college
        WHERE id = NEW.college_id AND is_deleted = 0
        LIMIT 1;

        SET NEW.college_name = v_college_name;
    END IF;

    -- 如果专业ID改变，更新专业名称
    IF NEW.major_id <> OLD.major_id AND NEW.major_id IS NOT NULL THEN
        SELECT name INTO v_major_name
        FROM t_major
        WHERE id = NEW.major_id AND is_deleted = 0
        LIMIT 1;

        SET NEW.major_name = v_major_name;
    END IF;

    -- 如果课程ID改变，更新课程名称
    IF NEW.course_id <> OLD.course_id AND NEW.course_id IS NOT NULL THEN
        SELECT name INTO v_course_name
        FROM t_course
        WHERE id = NEW.course_id AND is_deleted = 0
        LIMIT 1;

        SET NEW.course_name = v_course_name;
    END IF;

    -- 如果分类ID改变，更新分类名称
    IF NEW.category_id <> OLD.category_id AND NEW.category_id IS NOT NULL THEN
        SELECT name INTO v_category_name
        FROM t_category
        WHERE id = NEW.category_id AND is_deleted = 0
        LIMIT 1;

        SET NEW.category_name = v_category_name;
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table t_resource_request
-- ----------------------------
DROP TRIGGER IF EXISTS `set_names_before_insert_resource_request`;
delimiter ;;
CREATE TRIGGER `set_names_before_insert_resource_request` BEFORE INSERT ON `t_resource_request` FOR EACH ROW BEGIN
    DECLARE v_college_name VARCHAR(100);
    DECLARE v_major_name VARCHAR(100);
    DECLARE v_course_name VARCHAR(100);

    -- 设置学院名称
    IF NEW.college_id IS NOT NULL THEN
        SELECT name INTO v_college_name
        FROM t_college
        WHERE id = NEW.college_id AND is_deleted = 0
        LIMIT 1;

        SET NEW.college_name = v_college_name;
    END IF;

    -- 设置专业名称
    IF NEW.major_id IS NOT NULL THEN
        SELECT name INTO v_major_name
        FROM t_major
        WHERE id = NEW.major_id AND is_deleted = 0
        LIMIT 1;

        SET NEW.major_name = v_major_name;
    END IF;

    -- 设置课程名称
    IF NEW.course_id IS NOT NULL THEN
        SELECT name INTO v_course_name
        FROM t_course
        WHERE id = NEW.course_id AND is_deleted = 0
        LIMIT 1;

        SET NEW.course_name = v_course_name;
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table t_resource_request
-- ----------------------------
DROP TRIGGER IF EXISTS `set_category_name_before_insert_resource_request`;
delimiter ;;
CREATE TRIGGER `set_category_name_before_insert_resource_request` BEFORE INSERT ON `t_resource_request` FOR EACH ROW BEGIN
    DECLARE v_category_name VARCHAR(50);

    -- 设置分类名称
    IF NEW.category_id IS NOT NULL THEN
        SELECT name INTO v_category_name
        FROM t_category
        WHERE id = NEW.category_id AND is_deleted = 0
        LIMIT 1;

        SET NEW.category_name = v_category_name;
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table t_resource_request
-- ----------------------------
DROP TRIGGER IF EXISTS `set_names_before_update_resource_request`;
delimiter ;;
CREATE TRIGGER `set_names_before_update_resource_request` BEFORE UPDATE ON `t_resource_request` FOR EACH ROW BEGIN
    DECLARE v_college_name VARCHAR(100);
    DECLARE v_major_name VARCHAR(100);
    DECLARE v_course_name VARCHAR(100);

    -- 如果学院ID改变，更新学院名称
    IF NEW.college_id <> OLD.college_id AND NEW.college_id IS NOT NULL THEN
        SELECT name INTO v_college_name
        FROM t_college
        WHERE id = NEW.college_id AND is_deleted = 0
        LIMIT 1;

        SET NEW.college_name = v_college_name;
    END IF;

    -- 如果专业ID改变，更新专业名称
    IF NEW.major_id <> OLD.major_id AND NEW.major_id IS NOT NULL THEN
        SELECT name INTO v_major_name
        FROM t_major
        WHERE id = NEW.major_id AND is_deleted = 0
        LIMIT 1;

        SET NEW.major_name = v_major_name;
    END IF;

    -- 如果课程ID改变，更新课程名称
    IF NEW.course_id <> OLD.course_id AND NEW.course_id IS NOT NULL THEN
        SELECT name INTO v_course_name
        FROM t_course
        WHERE id = NEW.course_id AND is_deleted = 0
        LIMIT 1;

        SET NEW.course_name = v_course_name;
    END IF;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
