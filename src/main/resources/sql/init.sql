-- =============================================
-- 照片墙系统数据库初始化脚本
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS photo_wall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE photo_wall;

-- =============================================
-- 用户表
-- =============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` varchar(16) NOT NULL COMMENT '用户名',
    `password` varchar(128) NOT NULL COMMENT '密码（BCrypt加密）',
    `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
    `email` varchar(128) DEFAULT NULL COMMENT '邮箱',
    `avatar` varchar(256) DEFAULT NULL COMMENT '头像URL',
    `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` varchar(45) DEFAULT NULL COMMENT '最后登录IP',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =============================================
-- 角色表
-- =============================================
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_code` varchar(32) NOT NULL COMMENT '角色编码',
    `role_name` varchar(32) NOT NULL COMMENT '角色名称',
    `description` varchar(128) DEFAULT NULL COMMENT '角色描述',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- =============================================
-- 用户角色关联表
-- =============================================
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `role_id` bigint NOT NULL COMMENT '角色ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- =============================================
-- 登录日志表
-- =============================================
DROP TABLE IF EXISTS `login_log`;
CREATE TABLE `login_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id` bigint DEFAULT NULL COMMENT '用户ID（登录失败时可能为空）',
    `login_type` tinyint NOT NULL COMMENT '登录类型：1-手机验证码，2-邮箱验证码，3-密码',
    `login_ip` varchar(45) NOT NULL COMMENT '登录IP',
    `user_agent` varchar(512) DEFAULT NULL COMMENT '用户代理',
    `device_id` varchar(64) DEFAULT NULL COMMENT '设备ID',
    `device_name` varchar(128) DEFAULT NULL COMMENT '设备名称',
    `status` tinyint NOT NULL COMMENT '状态：0-失败，1-成功',
    `fail_reason` varchar(128) DEFAULT NULL COMMENT '失败原因',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_login_ip` (`login_ip`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- =============================================
-- 初始数据
-- =============================================

-- 初始角色数据
INSERT INTO `role` (`role_code`, `role_name`, `description`) VALUES
('ADMIN', '管理员', '系统管理员，拥有所有权限'),
('USER', '普通用户', '普通用户，拥有基础权限');

-- 初始管理员账号（密码：Admin@123456）
-- BCrypt加密后的密码：$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
INSERT INTO `user` (`username`, `password`, `nickname`, `status`) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '管理员', 1);

-- 关联管理员角色
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES
(1, 1);
